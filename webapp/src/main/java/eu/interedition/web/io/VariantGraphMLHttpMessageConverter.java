package eu.interedition.web.io;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphEdge;
import eu.interedition.collatex.graph.VariantGraphTransposition;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphMLHttpMessageConverter extends AbstractHttpMessageConverter<VariantGraph> {
  /**
   * The GraphML MIME type.
   */
  protected static final MediaType APPLICATION_XML_GRAPHML = new MediaType("application", "graphml+xml");

  private final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();

  public VariantGraphMLHttpMessageConverter() {
    super(APPLICATION_XML_GRAPHML);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return VariantGraph.class.isAssignableFrom(clazz);
  }

  @Override
  protected boolean canRead(MediaType mediaType) {
    return false;
  }

  @Override
  protected VariantGraph readInternal(Class<? extends VariantGraph> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new HttpMessageNotReadableException(clazz.toString());
  }

  @Override
  protected void writeInternal(VariantGraph graph, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    final OutputStream body = outputMessage.getBody();
    XMLStreamWriter xml = null;
    try {
      new SimpleVariantGraphSerializer(graph).toGraphML(xml = xmlOutputFactory.createXMLStreamWriter(body));
    } catch (XMLStreamException e) {
      throw new HttpMessageNotWritableException(e.getMessage(), e);
    } finally {
      if (xml != null) {
        try {
          xml.close();
        } catch (XMLStreamException e) {
        }
      }
      Closeables.closeQuietly(body);
    }
  }
}
