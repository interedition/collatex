package eu.interedition.text.json.map;

import eu.interedition.text.Annotation;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationSerializer extends JsonSerializer<Annotation> {

  public static final String NAME_FIELD = "n";
  public static final String RANGE_FIELD = "r";

  @Override
  public Class<Annotation> handledType() {
    return Annotation.class;
  }

  @Override
  public void serialize(Annotation value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeStartObject();
    jgen.writeObjectField(NAME_FIELD, value.getName());
    jgen.writeObjectField(RANGE_FIELD, value.getRange());
    jgen.writeEndObject();
  }
}
