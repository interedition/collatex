package eu.interedition.server.collatex;

import eu.interedition.collatex.graph.VariantGraph;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphSerializer extends JsonSerializer<VariantGraph> {
  @Override
  public void serialize(VariantGraph value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeStartObject();
    jgen.writeEndObject();
  }
}
