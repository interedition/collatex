/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.web.io;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IRow;

@Service
public class ApiObjectMapper extends ObjectMapper implements InitializingBean {

  @Override
  public void afterPropertiesSet() throws Exception {
    CustomSerializerFactory f = new CustomSerializerFactory();
    f.addGenericMapping(IAlignmentTable.class, new ApiAlignmentTableSerializer());
    setSerializerFactory(f);
  }

  private static class ApiAlignmentTableSerializer extends JsonSerializer<IAlignmentTable> {

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
}
