package eu.interedition.server.collatex;

import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.graph.VariantGraph;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollateXModule extends SimpleModule {

  public CollateXModule(GraphFactory graphFactory) {
    super(CollateXModule.class.getPackage().getName(), Version.unknownVersion());
    addDeserializer(Collation.class, new CollationDeserializer(graphFactory));
    addSerializer(VariantGraph.class, new VariantGraphSerializer());
  }
}
