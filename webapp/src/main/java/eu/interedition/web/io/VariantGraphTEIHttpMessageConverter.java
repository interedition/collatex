package eu.interedition.web.io;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Closeables;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.input.SimpleToken;
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
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphTEIHttpMessageConverter extends AbstractHttpMessageConverter<VariantGraph> {

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
      final Set<Witness> allWitnesses = graph.witnesses();

      xml = xmlOutputFactory.createXMLStreamWriter(body);
      xml.writeStartDocument();
      xml.writeStartElement("cx", "apparatus", COLLATEX_NS);
      xml.writeNamespace("cx", COLLATEX_NS);
      xml.writeNamespace("", TEI_NS);
      
      for (Iterator<Set<VariantGraphVertex>> rowIt = graph.join().rank().ranks().iterator(); rowIt.hasNext(); ) {
        final Set<VariantGraphVertex> row = rowIt.next();

        final SetMultimap<Witness, Token> tokenIndex = HashMultimap.create();
        for (VariantGraphVertex v : row) {
          for (Token token : v.tokens()) {
            tokenIndex.put(token.getWitness(), token);
          }
        }

        final SortedMap<Witness, String> cellContents = Maps.newTreeMap(Witness.SIGIL_COMPARATOR);
        for (Witness witness : tokenIndex.keySet()) {
          final StringBuilder cellContent = new StringBuilder();
          for (SimpleToken token : Ordering.natural().sortedCopy(Iterables.filter(tokenIndex.get(witness), SimpleToken.class))) {
            cellContent.append(token.getContent()).append(" ");
          }
          cellContents.put(witness, cellContent.toString().trim());
        }

        final SetMultimap<String, Witness> segments = LinkedHashMultimap.create();
        for (Map.Entry<Witness, String> cell : cellContents.entrySet()) {
          segments.put(cell.getValue(), cell.getKey());
        }

        final String firstSegment = Iterables.getFirst(segments.keySet(), "");
        if (segments.keySet().size() == 1 && segments.get(firstSegment).size() == allWitnesses.size()) {
          xml.writeCharacters(firstSegment);
        } else {
          xml.writeStartElement("", "app", TEI_NS);
          for (String segment : segments.keySet()) {
            final StringBuilder witnesses = new StringBuilder();
            for (Witness witness : segments.get(segment)) {
              witnesses.append(witness.getSigil()).append(" ");
            }
            xml.writeStartElement("", "rdg", TEI_NS);
            xml.writeAttribute("wit", witnesses.toString().trim());
            xml.writeCharacters(segment);
            xml.writeEndElement();
          }
          xml.writeEndElement();
        }
        
        if (rowIt.hasNext()) {
          xml.writeCharacters(" ");
        }
      }
      
      xml.writeEndElement();
      xml.writeEndDocument();
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
