package eu.interedition.collatex.io;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.*;

/**
 * Created by ronald on 5/6/15.
 * Based on code written by Gregor Middell.
 * Class is intended to be used as a singleton
 */
@Provider
@Produces("image/svg+xml")
public class VariantGraphSVGMessageBodyWriter implements MessageBodyWriter<VariantGraph> {
    private String dotPath;
    private final ExecutorService processThreads = Executors.newCachedThreadPool();

    public VariantGraphSVGMessageBodyWriter(String dotPath) {
        this.dotPath = dotPath;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return VariantGraph.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(VariantGraph variantGraph, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(VariantGraph graph, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        if (dotPath == null) {
            throw new WebApplicationException(Response.Status.NO_CONTENT);
        } else {
            final StringWriter dot = new StringWriter();
            new SimpleVariantGraphSerializer(graph).toDot(dot);

            final Process dotProc = new ProcessBuilder(dotPath, "-Grankdir=LR", "-Gid=VariantGraph", "-Tsvg").start();
            final StringWriter errors = new StringWriter();
            try {
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
                        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "image/svg+xml");
                        final byte[] buf = new byte[8192];
                        try (final InputStream in = dotProc.getInputStream(); final OutputStream out = entityStream) {
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
                            if (!dotProc.waitFor(1,TimeUnit.HOURS)) {
                                throw new CompletionException(new RuntimeException("dot processing took longer than 1 hour, process was timed out."));
                            }
                            if (dotProc.exitValue() != 0) {
                                throw new CompletionException(new IllegalStateException(errors.toString()));
                            }
                        } catch (InterruptedException e) {
                            throw new CompletionException(e);
                        }
                    }, processThreads)
                ).exceptionally(t -> {
                    throw new WebApplicationException(t);
                }).get();
            } catch(InterruptedException|ExecutionException e) {
                throw new WebApplicationException(e);
            }
        }
    }
}
