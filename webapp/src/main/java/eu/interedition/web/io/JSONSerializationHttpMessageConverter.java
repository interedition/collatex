package eu.interedition.web.io;

import com.google.common.io.Closeables;
import eu.interedition.text.json.JSONSerializer;
import eu.interedition.web.text.JSONSerialization;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JSONSerializationHttpMessageConverter extends AbstractHttpMessageConverter<JSONSerialization> {

  private final JSONSerializer jsonSerializer;
  private final JsonFactory jsonFactory;

  public JSONSerializationHttpMessageConverter(JSONSerializer jsonSerializer, ObjectMapper objectMapper) {
    super(MediaType.APPLICATION_JSON);
    this.jsonSerializer = jsonSerializer;
    this.jsonFactory = objectMapper.getJsonFactory();
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return JSONSerialization.class.isAssignableFrom(clazz);
  }

  @Override
  protected boolean canRead(MediaType mediaType) {
    return false;
  }

  @Override
  protected JSONSerialization readInternal(Class<? extends JSONSerialization> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void writeInternal(JSONSerialization serialization, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    final JsonGenerator jg = jsonFactory.createJsonGenerator(outputMessage.getBody());
    try {
      jsonSerializer.serialize(jg, serialization.getText(), serialization);
    } finally {
      Closeables.close(jg, false);
    }
  }
}
