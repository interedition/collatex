/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.tools;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleCollation;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import eu.interedition.collatex.simple.SimpleWitness;
import org.apache.commons.cli.CommandLine;
import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.http.server.accesslog.AccessLogAppender;
import org.glassfish.grizzly.http.server.accesslog.AccessLogProbe;
import org.glassfish.grizzly.http.server.accesslog.ApacheLogFormat;
import org.glassfish.grizzly.http.util.Header;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class CollationServer {
    private static final Logger LOG = Logger.getLogger(CollationServer.class.getName());

    private final int maxCollationSize;
    private final String dotPath;
    private final ExecutorService collationThreads;
    private final ExecutorService processThreads = Executors.newCachedThreadPool();

    public CollationServer(int maxParallelCollations, int maxCollationSize, String dotPath) {
        this.collationThreads = Executors.newFixedThreadPool(maxParallelCollations, new ThreadFactory() {
            private final AtomicLong counter = new AtomicLong();

            @Override
            public Thread newThread(Runnable r) {
                final Thread t = new Thread(r, "collator-" + counter.incrementAndGet());
                t.setDaemon(true);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            }
        });

        this.maxCollationSize = maxCollationSize;
        this.dotPath = dotPath;
    }

    public static void start(CommandLine commandLine) {
        final CollationServer collator = new CollationServer(
            Integer.parseInt(commandLine.getOptionValue("mpc", "2")),
            Integer.parseInt(commandLine.getOptionValue("mcs", "0")),
            Optional.ofNullable(commandLine.getOptionValue("dot")).orElse(detectDotPath())
        );
        final String staticPath = System.getProperty("collatex.static.path", "");
        final HttpHandler httpHandler = staticPath.isEmpty() ? new CLStaticHttpHandler(CollationPipe.class.getClassLoader(), "/static/") {
            @Override
            protected void onMissingResource(Request request, Response response) throws Exception {
                collator.service(request, response);
            }
        } : new StaticHttpHandler(staticPath.replaceAll("/+$", "") + "/") {
            @Override
            protected void onMissingResource(Request request, Response response) throws Exception {
                collator.service(request, response);
            }
        };

        final NetworkListener httpListener = new NetworkListener("http", "0.0.0.0", Integer.parseInt(commandLine.getOptionValue("p", "7369")));

        final CompressionConfig compressionConfig = httpListener.getCompressionConfig();
        compressionConfig.setCompressionMode(CompressionConfig.CompressionMode.ON);
        compressionConfig.setCompressionMinSize(860); // http://webmasters.stackexchange.com/questions/31750/what-is-recommended-minimum-object-size-for-gzip-performance-benefits
        compressionConfig.setCompressableMimeTypes("application/javascript", "application/json", "application/xml", "text/css", "text/html", "text/javascript", "text/plain", "text/xml");

        final HttpServer httpServer = new HttpServer();
        final ServerConfiguration httpServerConfig = httpServer.getServerConfiguration();

        httpServer.addListener(httpListener);
        httpServerConfig.addHttpHandler(httpHandler, commandLine.getOptionValue("cp", "").replaceAll("/+$", "") + "/*");
        httpServerConfig.getMonitoringConfig().getWebServerConfig().addProbes(new AccessLogProbe(new StandardOutAccessLogAppender(), ApacheLogFormat.COMBINED));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Stopping HTTP server");
            httpServer.shutdown();
        }));

        try {
            httpServer.start();
            Thread.sleep(Long.MAX_VALUE);
        } catch (IOException | InterruptedException e) {
            LOG.log(Level.SEVERE, e, e::getMessage);
        }
    }

    public void service(Request request, Response response) throws Exception {
        final Deque<String> path = path(request);
        if (path.isEmpty() || !"collate".equals(path.pop())) {
            response.sendError(404);
            return;
        }

        final SimpleCollation collation = JsonProcessor.read(request.getInputStream());
        if (maxCollationSize > 0) {
            for (SimpleWitness witness : collation.getWitnesses()) {
                final int witnessLength = witness.getTokens().stream()
                        .filter(t -> t instanceof SimpleToken).map(t -> (SimpleToken) t).mapToInt(t -> t.getContent().length()).sum();
                if (witnessLength > maxCollationSize) {
                    response.sendError(413, "Request Entity Too Large");
                    return;
                }
            }
        }

        response.suspend(60, TimeUnit.SECONDS, new EmptyCompletionHandler<>());
        collationThreads.submit(() -> {
            try {
                final VariantGraph graph = new VariantGraph();
                collation.collate(graph);

                // CORS support
                response.setHeader("Access-Control-Allow-Origin", Optional.ofNullable(request.getHeader("Origin")).orElse("*"));
                response.setHeader("Access-Control-Allow-Methods", Optional.ofNullable(request.getHeader("Access-Control-Request-Method")).orElse("GET, POST, HEAD, OPTIONS"));
                response.setHeader("Access-Control-Allow-Headers", Optional.ofNullable(request.getHeader("Access-Control-Request-Headers")).orElse("Content-Type, Accept, X-Requested-With"));
                response.setHeader("Access-Control-Max-Age", "86400");
                response.setHeader("Access-Control-Allow-Credentials", "true");

                final String clientAccepts = Optional.ofNullable(request.getHeader(Header.Accept)).orElse("");

                if (clientAccepts.contains("text/plain")) {
                    response.setContentType("text/plain");
                    response.setCharacterEncoding("utf-8");
                    try (final Writer out = response.getWriter()) {
                        new SimpleVariantGraphSerializer(graph).toDot(out);
                    }
                    response.resume();

                } else if (clientAccepts.contains("application/tei+xml")) {
                    XMLStreamWriter xml = null;
                    try {
                        response.setContentType("application/tei+xml");
                        try (OutputStream responseStream = response.getOutputStream()) {
                            xml = XMLOutputFactory.newInstance().createXMLStreamWriter(responseStream);
                            xml.writeStartDocument();
                            new SimpleVariantGraphSerializer(graph).toTEI(xml);
                            xml.writeEndDocument();
                        } finally {
                            if (xml != null) {
                                xml.close();
                            }
                        }
                        response.resume();
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                } else if (clientAccepts.contains("application/graphml+xml")) {
                    XMLStreamWriter xml = null;
                    try {
                        response.setContentType("application/graphml+xml");
                        try (OutputStream responseStream = response.getOutputStream()) {
                            xml = XMLOutputFactory.newInstance().createXMLStreamWriter(responseStream);
                            xml.writeStartDocument();
                            new SimpleVariantGraphSerializer(graph).toGraphML(xml);
                            xml.writeEndDocument();
                        } finally {
                            if (xml != null) {
                                xml.close();
                            }
                        }
                        response.resume();
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                } else if (clientAccepts.contains("image/svg+xml")) {
                    if (dotPath == null) {
                        response.sendError(204);
                        response.resume();
                    } else {
                        final StringWriter dot = new StringWriter();
                        new SimpleVariantGraphSerializer(graph).toDot(dot);

                        final Process dotProc = new ProcessBuilder(dotPath, "-Grankdir=LR", "-Gid=VariantGraph", "-Tsvg").start();
                        final StringWriter errors = new StringWriter();
                        CompletableFuture.allOf(
                            CompletableFuture.runAsync(() -> {
                                final char[] buf = new char[8192];
                                try (final Reader errorStream = new InputStreamReader(dotProc.getErrorStream())) {
                                    int len;
                                    while ((len = errorStream.read(buf)) >= 0) {
                                        errors.write(buf, 0, len);
                                    }
                                } catch (IOException e) {
                                    throw new CompletionException(e);
                                }
                            }, processThreads),
                            CompletableFuture.runAsync(() -> {
                                try (final Writer dotProcStream = new OutputStreamWriter(dotProc.getOutputStream(), "UTF-8")) {
                                    dotProcStream.write(dot.toString());
                                } catch (IOException e) {
                                    throw new CompletionException(e);
                                }
                            }, processThreads),
                            CompletableFuture.runAsync(() -> {
                                response.setContentType("image/svg+xml");
                                final byte[] buf = new byte[8192];
                                try (final InputStream in = dotProc.getInputStream(); final OutputStream out = response.getOutputStream()) {
                                    int len;
                                    while ((len = in.read(buf)) >= 0) {
                                        out.write(buf, 0, len);
                                    }
                                } catch (IOException e) {
                                    throw new CompletionException(e);
                                }
                            }, processThreads),
                            CompletableFuture.runAsync(() -> {
                                try {
                                    if (dotProc.waitFor() != 0) {
                                        throw new CompletionException(new IllegalStateException(errors.toString()));
                                    }
                                } catch (InterruptedException e) {
                                    throw new CompletionException(e);
                                }
                            }, processThreads)
                        ).exceptionally(t -> {
                            t.printStackTrace();
                            return null;
                        }).thenRunAsync(response::resume, processThreads);
                    }
                } else {
                    response.setContentType("application/json");
                    try (final OutputStream responseStream = response.getOutputStream()) {
                        JsonProcessor.write(graph, responseStream);
                    }
                    response.resume();
                }
            } catch (IOException e) {
                // FIXME: ignored
            }
        });
    }

    private static Deque<String> path(Request request) {
        return Pattern.compile("/+").splitAsStream(Optional.ofNullable(request.getPathInfo()).orElse(""))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(ArrayDeque::new));
    }

    private static String detectDotPath() {
        for (String detectionCommand : new String[] { "which dot", "where dot.exe" }) {
            try {

                final Process process = Runtime.getRuntime().exec(detectionCommand);
                try (BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                    final CompletableFuture<Optional<String>> path = CompletableFuture.supplyAsync(() -> processReader.lines()
                        .map(String::trim)
                        .filter(l -> l.toLowerCase().contains("dot"))
                        .findFirst());
                    process.waitFor();
                    final String dotPath = path.get().get();
                    LOG.info(() -> "Detected GraphViz' dot at '" + dotPath + "'");
                    return dotPath;
                }
            } catch (Throwable t) {
                LOG.log(Level.FINE, detectionCommand, t);
            }
        }
        return null;
    }


    private static class StandardOutAccessLogAppender implements AccessLogAppender {


        @Override
        public void append(String accessLogEntry) throws IOException {
            System.out.println(accessLogEntry);
        }

        @Override
        public void close() throws IOException {
        }
    }
}
