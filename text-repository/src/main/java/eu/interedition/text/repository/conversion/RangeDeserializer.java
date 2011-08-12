package eu.interedition.text.repository.conversion;

import eu.interedition.text.Range;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeDeserializer extends JsonDeserializer<Range> {
  @Override
  public Range deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    throw new UnsupportedOperationException();
  }
}
