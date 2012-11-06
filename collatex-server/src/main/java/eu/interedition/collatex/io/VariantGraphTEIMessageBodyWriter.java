package eu.interedition.collatex.io;

import com.google.common.io.Closeables;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Provider
@Produces("application/tei+xml")
public class VariantGraphTEIMessageBodyWriter implements MessageBodyWriter<VariantGraph> {

  private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

  static {
    XML_OUTPUT_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return VariantGraph.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(VariantGraph variantGraph, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(VariantGraph variantGraph, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    final Transaction tx = variantGraph.getDatabase().beginTx();
    try {
      XMLStreamWriter xml = null;
      try {
        new SimpleVariantGraphSerializer(variantGraph).toTEI(xml = XML_OUTPUT_FACTORY.createXMLStreamWriter(entityStream));
      } catch (XMLStreamException e) {
        throw new IOException(e.getMessage(), e);
      } finally {
        try {
          xml.close();
        } catch (XMLStreamException e) {
        }
        Closeables.close(entityStream, false);
      }
    } finally {
      tx.finish();
    }
  }
}
