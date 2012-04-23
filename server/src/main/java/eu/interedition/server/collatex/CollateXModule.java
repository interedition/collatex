package eu.interedition.server.collatex;

import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.graph.VariantGraph;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class CollateXModule extends SimpleModule implements InitializingBean {

  @Autowired
  private VariantGraphSerializer variantGraphSerializer;

  @Autowired
  private CollationDeserializer collationDeserializer;

  public CollateXModule() {
    super(CollateXModule.class.getPackage().getName(), Version.unknownVersion());
  }


  @Override
  public void afterPropertiesSet() throws Exception {
    addDeserializer(Collation.class, collationDeserializer);
    addSerializer(VariantGraph.class, variantGraphSerializer);
  }
}
