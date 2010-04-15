package eu.interedition.collatex2.spring;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IRow;

public class ApiAlignmentTableSerializer extends JsonSerializer<IAlignmentTable> {

  @Override
  public void serialize(IAlignmentTable value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeStartArray();
    for (IRow row : value.getRows()) {
      jgen.writeStartObject();
      jgen.writeStringField("witness", row.getSigil());
      jgen.writeFieldName("tokens");
      jgen.writeStartArray();
      for (ICell cell : row) {
        if (cell.isEmpty()) {
          jgen.writeNull();
        } else {
          provider.defaultSerializeValue(cell.getToken(), jgen);
        }
      }
      jgen.writeEndArray();
      jgen.writeEndObject();
    }
    jgen.writeEndArray();
  }

}
