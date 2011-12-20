package eu.interedition.collatex.cocoon;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.input.SimpleToken;
import eu.interedition.collatex.input.SimpleWitness;
import eu.interedition.collatex.input.WhitespaceTokenizer;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.output.Apparatus;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollateXTransformer extends AbstractSAXTransformer {

  private static final String TEI_NS = "http://www.tei-c.org/ns/1.0";
  public static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";

  private enum OutputType {
    ALIGNMENT_TABLE, TEI_APPARATUS
  }

  private GraphFactory graphFactory;
  private OutputType outputType = OutputType.ALIGNMENT_TABLE;
  private List<Iterable<Token>> witnesses = Lists.newArrayList();
  private String sigil;

  public CollateXTransformer() {
    super();
    try {
      this.graphFactory = GraphFactory.create();
      this.defaultNamespaceURI = COLLATEX_NS;
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public void startTransformingElement(String uri, String name, String raw, Attributes attr) throws ProcessingException,
          IOException, SAXException {
    if ("collation".equals(name)) {
      final String outputType = attr.getValue(defaultNamespaceURI, "outputType");
      if (outputType != null && "tei".equalsIgnoreCase(outputType.trim())) {
        this.outputType = OutputType.TEI_APPARATUS;
      } else {
        this.outputType = OutputType.ALIGNMENT_TABLE;
      }
      sigil = null;
      witnesses.clear();
    } else if ("witness".equals(name)) {
      sigil = attr.getValue("sigil");
      if (sigil == null) {
        sigil = "w" + (witnesses.size() + 1);
      }
      startTextRecording();
    }
  }

  @Override
  public void endTransformingElement(String uri, String name, String raw) throws ProcessingException, IOException, SAXException {
    if ("collation".equals(name)) {
      ignoreHooksCount++;
      switch (outputType) {
        case TEI_APPARATUS:
          sendTeiApparatus();
          break;
        default:
          sendAlignmentTable();
          break;
      }
      ignoreHooksCount--;
    } else if ("witness".equals(name)) {
      witnesses.add(new SimpleWitness(sigil, endTextRecording(), new WhitespaceTokenizer()));
    }
  }

  private void sendAlignmentTable() throws SAXException {
    sendStartElementEventNS("alignment", EMPTY_ATTRIBUTES);
    if (!witnesses.isEmpty()) {
      
      final VariantGraph graph = graphFactory.newVariantGraph();
      CollationAlgorithmFactory.dekker(new EqualityTokenComparator()).collate(graph, witnesses);
      final SortedSet<Witness> witnesses = graph.witnesses();
      final RowSortedTable<Integer, Witness, SortedSet<Token>> table = graph.toTable();
      for (Integer rowIndex : table.rowKeySet()) {
        final Map<Witness, SortedSet<Token>> row = table.row(rowIndex);
        sendStartElementEventNS("row", EMPTY_ATTRIBUTES);
        for (Witness witness : witnesses) {
          final AttributesImpl cellAttrs = new AttributesImpl();
          cellAttrs.addCDATAAttribute(namespaceURI, "sigil", "sigil", witness.getSigil());
          sendStartElementEventNS("cell", cellAttrs);
          if (!row.containsKey(witness)) {
            sendTextEvent(SimpleToken.toString(row.get(witness)));
          }
          sendEndElementEventNS("cell");

        }
        sendEndElementEventNS("row");
      }
    }
    sendEndElementEventNS("alignment");
  }

  private void sendTeiApparatus() throws SAXException {
    final Apparatus apparatus = null; // FIXME: create TEI-P5 output

    sendStartElementEventNS("apparatus", EMPTY_ATTRIBUTES);
    startPrefixMapping("tei", TEI_NS);
    // FIXME: this should be dealt with on the tokenizer level!
    final String separator = " ";
    for (Iterator<Apparatus.Entry> entryIt = apparatus.getEntries().iterator(); entryIt.hasNext(); ) {
      final Apparatus.Entry entry = entryIt.next();
      // group together similar phrases
      final Multimap<String, String> content2WitMap = ArrayListMultimap.create();
      for (Witness witness : entry.getWitnesses()) {
        content2WitMap.put(SimpleToken.toString(entry.getReadingOf(witness)), witness.getSigil());
      }

      if ((content2WitMap.keySet().size() == 1) && !entry.hasEmptyCells()) {
        // common content, there is no apparatus tag needed, just output the
        // segment
        sendTextEvent(content2WitMap.keys().iterator().next());
      } else {
        // convert the multimap to a normal map indexed by segment content and
        // containing a sorted set of witness identifiers
        Map<String, SortedSet<String>> readings = Maps.newHashMap();
        for (final String content : content2WitMap.keySet()) {
          readings.put(content, Sets.newTreeSet(content2WitMap.get(content)));
        }

        SortedMap<String, String> readingMap = Maps.newTreeMap();
        for (Map.Entry<String, SortedSet<String>> reading : readings.entrySet()) {
          readingMap.put(Joiner.on(" ").join(Iterables.transform(reading.getValue(), WIT_TO_XML_ID)), reading.getKey());
        }

        startElement(TEI_NS, "app", "tei:app", EMPTY_ATTRIBUTES);
        for (Map.Entry<String, String> reading : readingMap.entrySet()) {
          final AttributesImpl attributes = new AttributesImpl();
          attributes.addCDATAAttribute("wit", reading.getKey());
          startElement(TEI_NS, "rdg", "tei:rdg", attributes);
          String content = reading.getValue();
          if (!content.isEmpty()) {
            sendTextEvent(content);
          }
          endElement(TEI_NS, "rdg", "tei:rdg");
        }
        endElement(TEI_NS, "app", "tei:app");
      }
      if (entryIt.hasNext()) {
        sendTextEvent(separator);
      }
    }

    endPrefixMapping("tei");
    sendEndElementEventNS("apparatus");
  }

  private static final Function<String, String> WIT_TO_XML_ID = new Function<String, String>() {

    @Override
    public String apply(String from) {
      return (from.startsWith("#") ? from : ("#" + from));
    }
  };

}