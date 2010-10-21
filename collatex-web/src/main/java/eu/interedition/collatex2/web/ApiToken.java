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

package eu.interedition.collatex2.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;

@JsonSerialize(using = ApiToken.Serializer.class)
@JsonIgnoreProperties( { "position", "sigil" })
public class ApiToken extends NormalizedToken {
  private Map<String, Object> metadata;

  public ApiToken() {
    super();
  }
  
  public ApiToken(INormalizedToken other) {
    super(other);
  }

  @Override
  @JsonProperty("t")
  public void setContent(String content) {
    super.setContent(content);
  }

  @Override
  @JsonProperty("n")
  public void setNormalized(String normalized) {
    super.setNormalized(normalized);
  }
  
  @JsonAnySetter
  public void metadata(String key, Object value) {
    if (metadata == null) {
      metadata = new LinkedHashMap<String, Object>();
    }
    metadata.put(key, value);
  }

  public static class Serializer extends JsonSerializer<ApiToken> {

    @Override
    public void serialize(ApiToken value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeStringField("t", value.getContent());
      jgen.writeStringField("n", value.getNormalized());
      if (value.metadata != null) {
        for (String key : value.metadata.keySet()) {
          Object md = value.metadata.get(key);
          if (md == null) {
            jgen.writeNullField(key);
          } else {
            jgen.writeFieldName(key);
            provider.defaultSerializeValue(md, jgen);
          }
        }
      }
      jgen.writeEndObject();
    }

  }
}
