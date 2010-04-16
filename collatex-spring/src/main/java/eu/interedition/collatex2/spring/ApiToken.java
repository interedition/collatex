package eu.interedition.collatex2.spring;

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

import eu.interedition.collatex2.input.NormalizedToken;

@JsonSerialize(using = ApiToken.Serializer.class)
@JsonIgnoreProperties( { "position", "sigil" })
public class ApiToken extends NormalizedToken {
  private Map<String, Object> metadata;

  @Override
  @JsonProperty("token")
  public String getContent() {
    return super.getContent();
  }

  @Override
  @JsonProperty("token")
  public void setContent(String content) {
    super.setContent(content);
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
      jgen.writeStringField("token", value.getContent());
      jgen.writeStringField("normalized", value.getNormalized());
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
