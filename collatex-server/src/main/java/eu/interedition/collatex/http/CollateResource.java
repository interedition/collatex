package eu.interedition.collatex.http;

import com.google.common.collect.Iterables;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.io.Collation;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import freemarker.template.Configuration;
import freemarker.template.Template;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
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

  private final Configuration templates;

  private final int maxCollationSize;
  private final ExecutorService executor;

  public CollateResource(Configuration templates, int maxParallelCollations, int maxCollationSize) {
    this.templates = templates;
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
  public Template index() throws IOException {
    return templates.getTemplate("index.ftl");
  }

  @GET
  @Path("collate/apidocs")
  public Template apidocs() throws IOException {
    return templates.getTemplate("collate/apidocs.ftl");
  }

  @GET
  @Path("collate/console")
  public Template console() throws IOException {
    return templates.getTemplate("collate/console.ftl");
  }

  @GET
  @Path("collate/darwin")
  public Template darwin() throws IOException {
    return templates.getTemplate("collate/darwin-example.ftl");
  }


  @Path("collate")
  @GET
  public Response redirectToDocs(@Context UriInfo uriInfo) throws NoSuchMethodException {
    return Response.seeOther(uriInfo.getBaseUriBuilder().path(getClass().getMethod("apidocs")).build()).build();
  }

  @Path("collate")
  @POST
  public VariantGraph collate(final Collation collation) throws ExecutionException, InterruptedException {
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

    return executor.submit(new Callable<VariantGraph>() {
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
    }).get();
  }
}