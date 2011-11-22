package eu.interedition.text.json.map;

import eu.interedition.text.Range;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

import static org.codehaus.jackson.JsonToken.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeDeserializer extends JsonDeserializer<Range> {
  @Override
  public Range deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    if (!START_ARRAY.equals(jp.getCurrentToken())) {
      throw new JsonParseException("QName: Expected start of array", jp.getCurrentLocation());
    }

    JsonToken token = jp.nextToken();
    if (!VALUE_NUMBER_INT.equals(token)) {
      throw new JsonParseException("QName: Expected number as start of range", jp.getCurrentLocation());
    }

    final long start = jp.getValueAsLong();

    token = jp.nextToken();
    if (!VALUE_NUMBER_INT.equals(token)) {
      throw new JsonParseException("QName: Expected number as end of range", jp.getCurrentLocation());
    }
    final Range range = new Range(start, jp.getValueAsLong());

    if (!END_ARRAY.equals(jp.nextToken())) {
      throw new JsonParseException("QName: Expected end of array", jp.getCurrentLocation());
    }

    return range;
  }
}
