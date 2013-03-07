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

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.sun.jersey.api.NotFoundException;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.io.Collation;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleToken;

import javax.ws.rs.GET;
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
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */

@Path("/")
public class CollateResource {

  private final File staticPath;
  private final Date initial = new Date();

  private final int maxCollationSize;
  private final ExecutorService executor;

  public CollateResource(String staticPath, int maxParallelCollations, int maxCollationSize) {
    this.staticPath = (Strings.isNullOrEmpty(staticPath) ? null : new File(staticPath));
    this.maxCollationSize = maxCollationSize;
    this.executor = Executors.newFixedThreadPool(maxParallelCollations, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        final Thread t = new Thread(r, CollateResource.class.getName());
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
      }
    });
  }

  @GET
  public Response index(@Context Request request, @Context UriInfo uriInfo) throws IOException {
    if (!uriInfo.getRequestUri().getPath().endsWith("/")) {
      return Response.seeOther(uriInfo.getBaseUriBuilder().path("/").build()).build();
    }
    return stream(request, "index.html");
  }

  @GET
  @Path("darwin")
  public Response darwin(@Context Request request) throws IOException {
    return stream(request, "darwin.html");
  }


  @Path("collate")
  @GET
  public Response redirectToIndex(@Context HttpHeaders hh, @Context UriInfo uriInfo) throws NoSuchMethodException {
    return corsSupport(hh, Response.seeOther(uriInfo.getBaseUriBuilder().path("/").build())).build();
  }

  @Path("collate")
  @OPTIONS
  public Response collateOptions(@Context HttpHeaders hh) {
    return corsSupport(hh, Response.ok()).build();
  }

  @Path("collate")
  @POST
  public Response collate(final Collation collation, @Context HttpHeaders hh) throws ExecutionException, InterruptedException {
    if (maxCollationSize > 0) {
      int witnessLength = 0;
      for (Iterable<Token> witness : collation.getWitnesses()) {
        for (SimpleToken token : Iterables.filter(witness, SimpleToken.class)) {
          witnessLength += token.getContent().length();

        }
        if (witnessLength > maxCollationSize) {
          throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
      }
    }

    return corsSupport(hh, Response.ok(executor.submit(new Callable<VariantGraph>() {
      @Override
      public VariantGraph call() throws Exception {
        VariantGraph graph = new JungVariantGraph();

        if (collation != null) {
          // merge
          collation.getAlgorithm().collate(graph, collation.getWitnesses());

          // post-process
          if (collation.isJoined()) {
            graph = VariantGraph.JOIN.apply(graph);
          }
        }

        return graph;
      }
    }).get())).build();
  }

  Response.ResponseBuilder corsSupport(@Context HttpHeaders hh, Response.ResponseBuilder response) {
    final MultivaluedMap<String, String> requestHeaders = hh.getRequestHeaders();
    return response.header("Access-Control-Allow-Origin", Objects.firstNonNull(requestHeaders.getFirst("Origin"), "*"))
            .header("Access-Control-Allow-Methods", Objects.firstNonNull(requestHeaders.getFirst("Access-Control-Request-Method"), "GET, POST, HEAD, OPTIONS"))
            .header("Access-Control-Allow-Headers", Objects.firstNonNull(requestHeaders.getFirst("Access-Control-Request-Headers"), "Content-Type, Accept, X-Requested-With"))
            .header("Access-Control-Max-Age", "86400")
            .header("Access-Control-Allow-Credentials", "true");
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
        Closeables.close(stream, false);
        throw new WebApplicationException(preconditions.build());
      }
    }

    final String extension = Files.getFileExtension(path);
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
}