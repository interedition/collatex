/*
 * Copyright (c) 2013 The Interedition Development Group.
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

package eu.interedition.collatex.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleCollation;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */

@Path("/")
public class CollateResource {

    private final File staticPath;
    private final Date initial = new Date();

    private final int maxCollationSize;
    private final ExecutorService executor;

    public CollateResource(String staticPath, int maxParallelCollations, int maxCollationSize) {
        this.staticPath = staticPath == null || "".equals(staticPath) ? null : new File(staticPath);
        this.maxCollationSize = maxCollationSize;
        this.executor = Executors.newFixedThreadPool(maxParallelCollations, r -> {
            final Thread t = new Thread(r, CollateResource.class.getName());
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
    }

    @GET
    public Response index(@Context Request request) throws IOException {
        return stream(request, "index.html");
    }

    @GET
    @Path("darwin")
    public Response darwin(@Context Request request) throws IOException {
        return stream(request, "darwin.html");
    }


    @Path("collate")
    @GET
    public Response noContent(@Context HttpHeaders hh) throws NoSuchMethodException {
        return corsSupport(hh, Response.noContent()).build();
    }

    @Path("collate")
    @OPTIONS
    public Response collateOptions(@Context HttpHeaders hh) {
        return corsSupport(hh, Response.ok()).build();
    }

    @Path("collate")
    @POST
    public Response collate(final SimpleCollation collation, @Context HttpHeaders hh) throws ExecutionException, InterruptedException {
        if (maxCollationSize > 0) {
            for (SimpleWitness witness : collation.getWitnesses()) {
                final int witnessLength = witness.getTokens().stream()
                        .filter(t -> t instanceof SimpleToken).map(t -> (SimpleToken) t).mapToInt(t -> t.getContent().length()).sum();
                if (witnessLength > maxCollationSize) {
                    return Response.status(new Response.StatusType() {
                        @Override
                        public int getStatusCode() {
                            return 413;
                        }

                        @Override
                        public Response.Status.Family getFamily() {
                            return Response.Status.Family.CLIENT_ERROR;
                        }

                        @Override
                        public String getReasonPhrase() {
                            return "Request Entity Too Large";
                        }
                    }).build();
                }
            }
        }

        return corsSupport(hh, Response.ok(executor.submit(() -> {
            final VariantGraph graph = new VariantGraph();
            return (collation == null ? graph : collation.collate(graph));
        }).get())).build();
    }

    Response.ResponseBuilder corsSupport(@Context HttpHeaders hh, Response.ResponseBuilder response) {
        // CORS support
        final MultivaluedMap<String, String> requestHeaders = hh.getRequestHeaders();
        response.header("Access-Control-Allow-Origin", Optional.ofNullable(requestHeaders.getFirst("Origin")).orElse("*"));
        response.header("Access-Control-Allow-Methods", Optional.ofNullable(requestHeaders.getFirst("Access-Control-Request-Method")).orElse("GET, POST, HEAD, OPTIONS"));
        response.header("Access-Control-Allow-Headers", Optional.ofNullable(requestHeaders.getFirst("Access-Control-Request-Headers")).orElse("Content-Type, Accept, X-Requested-With"));
        response.header("Access-Control-Max-Age", "86400");
        response.header("Access-Control-Allow-Credentials", "true");
        return response;
    }

    @Path("{path: .+?\\.((html)|(css)|(js)|(png)|(ico))}")
    @GET
    public Response stream(@Context Request request, @PathParam("path") String path) throws IOException {
        InputStream stream = null;
        Date lastModified = initial;
        if (staticPath == null) {
            stream = getClass().getResourceAsStream("/static/" + path);
        } else {
            final File file = new File(staticPath, path);
            if (file.isFile() && file.getCanonicalPath().startsWith(staticPath.getCanonicalPath())) {
                stream = new FileInputStream(file);
                lastModified = new Date(file.lastModified());
            }
        }

        if (stream == null) {
            throw new NotFoundException();
        }

        if (request.getMethod().equals("GET")) {
            final Response.ResponseBuilder preconditions = request.evaluatePreconditions(lastModified);
            if (preconditions != null) {
                try {
                    stream.close();
                } catch(IOException ignored) { }
                throw new WebApplicationException(preconditions.build());
            }
        }

        final String extension = getExtension(path);
        String contentType = "application/octet-stream";
        if ("html".equals(extension)) {
            contentType = "text/html";
        } else if ("js".equals(extension)) {
            contentType = "text/javascript";
        } else if ("css".equals(extension)) {
            contentType = "text/css";
        } else if ("png".equals(extension)) {
            contentType = "image/png";
        } else if ("ico".equals(extension)) {
            contentType = "image/x-icon";
        }

        return Response.ok()
            .entity(stream)
            .lastModified(lastModified)
            .type(contentType)
            .build();
    }

    private String getExtension(String path) {
        int lastIndexOf = path.lastIndexOf(".");
        if(lastIndexOf != -1 && lastIndexOf != 0)
            return path.substring(lastIndexOf + 1);
        else return "";
    }
}