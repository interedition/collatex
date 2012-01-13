package eu.interedition.web.io;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Closeables;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import org.neo4j.graphdb.Transaction;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphTEIHttpMessageConverter extends AbstractHttpMessageConverter<VariantGraph> {

  /**
   * TEI MIME type.
   */
  protected static final MediaType APPLICATION_TEI_XML = new MediaType("application", "tei+xml");

  private final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();

  public VariantGraphTEIHttpMessageConverter() {
    super(APPLICATION_TEI_XML);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return VariantGraph.class.isAssignableFrom(clazz);
  }

  @Override
  protected VariantGraph readInternal(Class<? extends VariantGraph> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new HttpMessageNotReadableException(clazz.toString());
  }

  @Override
  protected void writeInternal(VariantGraph graph, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    final Transaction tx = graph.newTransaction();
    final OutputStream body = outputMessage.getBody();
    XMLStreamWriter xml = null;
    try {
      new SimpleVariantGraphSerializer(graph).toTEI(xml = xmlOutputFactory.createXMLStreamWriter(body));
      tx.success();
    } catch (XMLStreamException e) {
      throw new HttpMessageNotWritableException(e.getMessage(), e);
    } finally {
      tx.finish();
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
