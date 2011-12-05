package eu.interedition.collatex.web.io;

import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphTEIHttpMessageConverter extends AbstractHttpMessageConverter<PersistentVariantGraph> {

  /**
   * TEI MIME type.
   */
  protected static final MediaType APPLICATION_TEI_XML = new MediaType("application", "tei+xml");

  /**
   * CollateX custom namespace.
   */
  protected static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";

  /**
   * The TEI P5 namespace.
   */
  protected static final String TEI_NS = "http://www.tei-c.org/ns/1.0";
  private final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();

  public VariantGraphTEIHttpMessageConverter() {
    super(APPLICATION_TEI_XML);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return PersistentVariantGraph.class.isAssignableFrom(clazz);
  }

  @Override
  protected PersistentVariantGraph readInternal(Class<? extends PersistentVariantGraph> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new HttpMessageNotReadableException(clazz.toString());
  }

  @Override
  protected void writeInternal(PersistentVariantGraph graph, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

  }
}
