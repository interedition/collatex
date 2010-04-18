package eu.interedition.collatex2.output;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Apparatus element serializing to the output format specified in ticket #6.
 * 
 */

public class TeiParallelSegmentationApparatusBuilder {
  public static final String TEI_NS = "http://www.tei-c.org/ns/1.0";

  public static void build(ParallelSegmentationApparatus apparatus, Node parent) {
    Document doc = (parent.getNodeType() == Node.DOCUMENT_NODE ? (Document) parent : parent.getOwnerDocument());
    for (final ApparatusEntry entry : apparatus.getEntries()) {
      // group together similar phrases
      final Multimap<String, String> content2WitMap = ArrayListMultimap.create();
      for (String sigel : entry.getSigli()) {
        content2WitMap.put(entry.getPhrase(sigel).getContent(), sigel);
      }

      if ((content2WitMap.keySet().size() == 1) && !entry.hasEmptyCells()) {
        // common content, there is no apparatus tag needed, just output the
        // segment
        parent.appendChild(doc.createTextNode(content2WitMap.keys().iterator().next()));
      } else {
        // convert the multimap to a normal map indexed by segment content and
        // containing a sorted set of witness identifiers
        Map<String, SortedSet<String>> readings = Maps.newHashMap();
        for (final String content : content2WitMap.keySet()) {
          readings.put(content, Sets.newTreeSet(content2WitMap.get(content)));
        }

        SortedMap<String, String> readingMap = Maps.newTreeMap();
        for (Entry<String, SortedSet<String>> reading : readings.entrySet()) {
          readingMap.put(Joiner.on(" ").join(Iterables.transform(reading.getValue(), WIT_TO_XML_ID)), reading.getKey());
        }

        Element app = doc.createElementNS(TEI_NS, "app");
        parent.appendChild(app);
        for (Entry<String, String> reading : readingMap.entrySet()) {
          Element rdg = doc.createElementNS(TEI_NS, "rdg");
          app.appendChild(rdg);
          rdg.setAttribute("wit", reading.getKey());
          String content = reading.getValue();
          if (!content.isEmpty()) {
            rdg.setTextContent(content);
          }
        }
      }
    }
  }

  private static final Function<String, String> WIT_TO_XML_ID = new Function<String, String>() {

    @Override
    public String apply(String from) {
      return (from.startsWith("#") ? from : ("#" + from));
    }
  };
}
