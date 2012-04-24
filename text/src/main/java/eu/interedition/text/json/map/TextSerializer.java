package eu.interedition.text.json.map;

import eu.interedition.text.Text;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextSerializer extends JsonSerializer<Text> {
  private static final String TYPE_FIELD = "type";
  private static final String LENGTH_FIELD = "len";
  private static final String ID_FIELD = "id";

  @Override
  public Class<Text> handledType() {
    return Text.class;
  }

  @Override
  public void serialize(Text value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeStartObject();
    jgen.writeStringField(TYPE_FIELD, value.getType().name().toLowerCase());
    jgen.writeNumberField(LENGTH_FIELD, value.getLength());
    jgen.writeNumberField(ID_FIELD, value.getId());
    jgen.writeEndObject();
  }
}
