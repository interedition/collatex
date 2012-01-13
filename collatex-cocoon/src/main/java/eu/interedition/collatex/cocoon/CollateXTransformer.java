package eu.interedition.collatex.cocoon;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.SetMultimap;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.simple.WhitespaceTokenizer;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.neo4j.graphdb.Transaction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

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

  @Override
  public void configure(Configuration configuration) throws ConfigurationException {
    super.configure(configuration);
    this.defaultNamespaceURI = COLLATEX_NS;
    try {
      this.graphFactory = GraphFactory.create();
    } catch (IOException e) {
      throw new ConfigurationException("I/O error while creating variant graph factory", configuration, e);
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
    if ("collation".equals(name) && !witnesses.isEmpty()) {
      ignoreHooksCount++;
      final Transaction tx = graphFactory.getDatabase().beginTx();
      try {
        final VariantGraph graph = graphFactory.newVariantGraph();
        CollationAlgorithmFactory.dekker(new EqualityTokenComparator()).collate(graph, witnesses);
        switch (outputType) {
          case TEI_APPARATUS:
            sendTeiApparatus(graph);
            break;
          default:
            sendAlignmentTable(graph);
            break;
        }
      } finally {
        tx.finish();
      }
      ignoreHooksCount--;
    } else if ("witness".equals(name)) {
      witnesses.add(new SimpleWitness(sigil, endTextRecording(), new WhitespaceTokenizer()));
    }
  }

  private void sendAlignmentTable(VariantGraph graph) throws SAXException {
    sendStartElementEventNS("alignment", EMPTY_ATTRIBUTES);
    final Set<Witness> witnesses = graph.witnesses();
    final RowSortedTable<Integer, Witness, Set<Token>> table = graph.toTable();

    for (Integer rowIndex : table.rowKeySet()) {
      final Map<Witness, Set<Token>> row = table.row(rowIndex);
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
    sendEndElementEventNS("alignment");
  }

  private void sendTeiApparatus(VariantGraph graph) throws SAXException {
    final Set<Witness> allWitnesses = graph.witnesses();

    sendStartElementEventNS("apparatus", EMPTY_ATTRIBUTES);
    startPrefixMapping("tei", TEI_NS);

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
        sendTextEvent(firstSegment);
      } else {
        startElement(TEI_NS, "app", "tei:app", EMPTY_ATTRIBUTES);
        for (String segment : segments.keySet()) {
          final StringBuilder witnesses = new StringBuilder();
          for (Witness witness : segments.get(segment)) {
            witnesses.append(witness.getSigil()).append(" ");
          }
          final AttributesImpl attributes = new AttributesImpl();
          attributes.addCDATAAttribute("wit", witnesses.toString().trim());
          startElement(TEI_NS, "rdg", "tei:rdg", attributes);
          sendTextEvent(segment);
          endElement(TEI_NS, "rdg", "tei:rdg");
        }
        endElement(TEI_NS, "app", "tei:app");
      }

      if (rowIt.hasNext()) {
        sendTextEvent(" ");
      }
    }

    endPrefixMapping("tei");
    sendEndElementEventNS("apparatus");
  }
}