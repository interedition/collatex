package eu.interedition.web.io;

import eu.interedition.web.text.XMLSerialization;
import eu.interedition.text.xml.XMLSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLSerializationHttpMessageConverter extends AbstractHttpMessageConverter<XMLSerialization> {

  private final XMLSerializer xmlSerializer;
  private final SAXTransformerFactory transformerFactory;

  public XMLSerializationHttpMessageConverter(XMLSerializer xmlSerializer) {
    super(MediaType.APPLICATION_XML);
    this.xmlSerializer = xmlSerializer;
    this.transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return XMLSerialization.class.isAssignableFrom(clazz);
  }

  @Override
  protected boolean canRead(MediaType mediaType) {
    return false;
  }

  @Override
  protected XMLSerialization readInternal(Class<? extends XMLSerialization> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void writeInternal(XMLSerialization serialization, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    try {
      final TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
      transformerHandler.setResult(new StreamResult(outputMessage.getBody()));
      xmlSerializer.serialize(transformerHandler, serialization.getText(), serialization);
    } catch (TransformerConfigurationException e) {
      throw new IOException("XML error while serializing text", e);
    } catch (XMLStreamException e) {
      throw new IOException("XML error while serializing text", e);
    }
  }
}
