package eu.interedition.collatex.io;

import com.google.inject.Provider;
import eu.interedition.collatex.neo4j.Neo4jVariantGraph;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ObjectMapperProvider implements Provider<ObjectMapper> {

  @Override
  public ObjectMapper get() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new CollateXModule());
    return objectMapper;
  }

  public static class CollateXModule extends SimpleModule {

    public CollateXModule() {
      super(CollateXModule.class.getPackage().getName(), Version.unknownVersion());
      addDeserializer(Collation.class, new CollationDeserializer());
      addSerializer(Neo4jVariantGraph.class, new VariantGraphSerializer());
    }
  }
}
