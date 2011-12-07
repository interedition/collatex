package eu.interedition.collatex.web.io;

import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
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
public class VariantGraphMLHttpMessageConverter extends AbstractHttpMessageConverter<PersistentVariantGraph> {
  /**
   * The GraphML MIME type.
   */
  protected static final MediaType APPLICATION_XML_GRAPHML = new MediaType("application", "graphml+xml");


  public VariantGraphMLHttpMessageConverter() {
    super(APPLICATION_XML_GRAPHML);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return PersistentVariantGraph.class.isAssignableFrom(clazz);
  }

  @Override
  protected boolean canRead(MediaType mediaType) {
    return false;
  }

  @Override
  protected PersistentVariantGraph readInternal(Class<? extends PersistentVariantGraph> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new HttpMessageNotReadableException(clazz.toString());
  }

  @Override
  protected void writeInternal(PersistentVariantGraph graph, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    throw new UnsupportedOperationException();
  }
}
