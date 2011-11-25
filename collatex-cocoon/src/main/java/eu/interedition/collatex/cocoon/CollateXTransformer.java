package eu.interedition.collatex.cocoon;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import eu.interedition.collatex.implementation.CollateXEngine;
import eu.interedition.collatex.implementation.input.Token;
import eu.interedition.collatex.implementation.input.WhitespaceAndPunctuationTokenizer;
import eu.interedition.collatex.interfaces.*;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollateXTransformer extends AbstractSAXTransformer {

  private static final String TEI_NS = "http://www.tei-c.org/ns/1.0";
  public static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";

  private enum OutputType {
    ALIGNMENT_TABLE, TEI_APPARATUS
  }

  private CollateXEngine engine = new CollateXEngine();
  private OutputType outputType = OutputType.ALIGNMENT_TABLE;
  private List<IWitness> witnesses = Lists.newArrayList();
  private String sigil;

  public CollateXTransformer() {
    super();
    this.defaultNamespaceURI = COLLATEX_NS;
    engine.setTokenizer(new WhitespaceAndPunctuationTokenizer());
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
      witnesses.add(engine.createWitness(sigil, endTextRecording()));
    }
  }

  private void sendAlignmentTable() throws SAXException {
    sendStartElementEventNS("alignment", EMPTY_ATTRIBUTES);
    if (!witnesses.isEmpty()) {
      final IAlignmentTable alignmentTable = engine.align(witnesses.toArray(new IWitness[witnesses.size()]));
      for (IRow row : alignmentTable.getRows()) {
        final AttributesImpl rowAttrs = new AttributesImpl();
        rowAttrs.addAttribute(namespaceURI, "sigil", "sigil", "CDATA", row.getSigil());
        sendStartElementEventNS("row", rowAttrs);
        for (ICell cell : row) {
          final AttributesImpl cellAttrs = new AttributesImpl();
          cellAttrs.addCDATAAttribute(namespaceURI, "state", cell.getColumn().getState().toString().toLowerCase());
          sendStartElementEventNS("cell", cellAttrs);
          if (!cell.isEmpty()) {
            sendTextEvent(cell.getToken().getContent());
          }
          sendEndElementEventNS("cell");

        }
        sendEndElementEventNS("row");
      }
    }
    sendEndElementEventNS("alignment");
  }

  private void sendTeiApparatus() throws SAXException {
    final IApparatus apparatus = engine.createApparatus(engine.graph(witnesses.toArray(new IWitness[witnesses.size()])));

    sendStartElementEventNS("apparatus", EMPTY_ATTRIBUTES);
    startPrefixMapping("tei", TEI_NS);
    // FIXME: this should be dealt with on the tokenizer level!
    final String separator = " ";
    for (Iterator<IApparatusEntry> entryIt = apparatus.getEntries().iterator(); entryIt.hasNext(); ) {
      final IApparatusEntry entry = entryIt.next();
      // group together similar phrases
      final Multimap<String, String> content2WitMap = ArrayListMultimap.create();
      for (IWitness witness : entry.getWitnesses()) {
        content2WitMap.put(Token.toString(entry.getPhrase(witness)), witness.getSigil());
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