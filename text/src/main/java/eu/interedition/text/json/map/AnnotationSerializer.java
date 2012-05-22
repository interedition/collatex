package eu.interedition.text.json.map;

import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationSerializer extends JsonSerializer<Annotation> {

  public static final String ID_FIELD = "id";
  public static final String NAME_FIELD = "n";
  public static final String TARGET_FIELD = "t";
  private static final String DATA_FIELD = "d";

  @Override
  public Class<Annotation> handledType() {
    return Annotation.class;
  }

  @Override
  public void serialize(Annotation value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeStartObject();
    jgen.writeNumberField(ID_FIELD, value.getId());

    final Name name = value.getName();
    final long nameId = name.getId();
    jgen.writeFieldName(NAME_FIELD);
    if (nameId == 0) {
      jgen.writeObject(name);
    } else {
      jgen.writeNumber(nameId);
    }

    jgen.writeObjectField(TARGET_FIELD, value.getTargets());
    jgen.writeFieldName(DATA_FIELD);

    jgen.writeTree(value.getData());

    jgen.writeEndObject();
  }
}
