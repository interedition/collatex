package eu.interedition.text.repository.conversion;

import eu.interedition.text.Range;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeSerializer extends JsonSerializer<Range> {
  @Override
  public void serialize(Range value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeStartObject();
    jgen.writeNumberField("s", value.getStart());
    jgen.writeNumberField("e", value.getEnd());
    jgen.writeEndObject();
  }
}
