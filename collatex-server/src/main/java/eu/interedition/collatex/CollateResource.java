package eu.interedition.collatex;

import com.google.inject.Inject;
import eu.interedition.collatex.neo4j.GraphFactory;
import eu.interedition.collatex.io.Collation;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */

@Path("/")
public class CollateResource {

  private final Configuration templates;
  private final GraphFactory graphFactory;

  @Inject
  public CollateResource(Configuration templates, GraphFactory graphFactory) {
    this.templates = templates;
    this.graphFactory = graphFactory;
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
  public VariantGraph collate(Collation collation) {
    final Transaction tx = graphFactory.getDatabase().beginTx();
    try {
      // create
      final VariantGraph graph = graphFactory.newVariantGraph();

      if (collation != null) {
        // merge
        collation.getAlgorithm().collate(graph, collation.getWitnesses());

        // post-process
        if (collation.isJoined()) {
          graph.join();
        }
        graph.rank();
      }

      tx.success();
      return graph;
    } finally {
      tx.finish();
    }
  }
}