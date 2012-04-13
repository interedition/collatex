package eu.interedition.server.collatex;

import com.google.common.base.Preconditions;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.graph.VariantGraph;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VariantGraphResource extends ServerResource {

  @Autowired
  private GraphFactory graphFactory;

  @Autowired
  private Configuration freemarkerConfiguration;

  @Override
  protected void doInit() throws ResourceException {
    Preconditions.checkState(getRequest() != null);
  }

  @Get("html")
  public Template docs() throws IOException {
    return freemarkerConfiguration.getTemplate("test.ftl");
  }

  @Post
  public VariantGraph collate(Collation collation) throws IOException {
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

    return graph;
  }
}
