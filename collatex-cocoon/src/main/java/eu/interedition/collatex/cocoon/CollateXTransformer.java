/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.cocoon;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.SetMultimap;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.matching.EditDistanceTokenComparator;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleCollation;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.util.ParallelSegmentationApparatus;
import eu.interedition.collatex.util.VariantGraphRanking;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Comparator;
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

  private enum Format {
    ALIGNMENT_TABLE, TEI_APPARATUS
  }

  private Format format = Format.ALIGNMENT_TABLE;
  private CollationAlgorithm algorithm;
  private boolean joined;
  private final List<SimpleWitness> witnesses = Lists.newArrayList();
  private String sigil;

  @Override
  public void configure(Configuration configuration) throws ConfigurationException {
    super.configure(configuration);
    this.defaultNamespaceURI = COLLATEX_NS;
  }

  @Override
  public void startTransformingElement(String uri, String name, String raw, Attributes attr) throws ProcessingException, IOException, SAXException {
    if (!COLLATEX_NS.equals(uri)) {
      return;
    }
    if ("collation".equals(name)) {
      final String format = Objects.firstNonNull(attributeValue(attr, "format"), "table").trim().toLowerCase();
      if ("tei".equals(format)) {
        this.format = Format.TEI_APPARATUS;
      } else {
        this.format = Format.ALIGNMENT_TABLE;
      }

      Comparator<Token> tokenComparator = new EqualityTokenComparator();
      try {
        final int editDistance = Integer.parseInt(Objects.firstNonNull(attributeValue(attr, "editDistance"), "0"));
        if (editDistance > 0) {
          tokenComparator = new EditDistanceTokenComparator(editDistance);
        }
      } catch (NumberFormatException e) {
      }

      final String algorithm = Objects.firstNonNull(attributeValue(attr, "algorithm"), "dekker").trim().toLowerCase();
      if (algorithm.equals("medite")) {
        this.algorithm = CollationAlgorithmFactory.medite(tokenComparator, SimpleToken.TOKEN_MATCH_EVALUATOR);
      } else if (algorithm.equals("needleman-wunsch")) {
        this.algorithm = CollationAlgorithmFactory.needlemanWunsch(tokenComparator);
      } else {
        this.algorithm = CollationAlgorithmFactory.dekker(tokenComparator);
      }

      this.joined = "true".equals(Objects.firstNonNull(attributeValue(attr, "joined"), "true").trim().toLowerCase());

      sigil = null;
      witnesses.clear();
    } else if ("witness".equals(name)) {
      sigil = Objects.firstNonNull(attributeValue(attr, "sigil"), "w" + (witnesses.size() + 1));
      startTextRecording();
    }
  }

  @Override
  public void endTransformingElement(String uri, String name, String raw) throws ProcessingException, IOException, SAXException {
    if (!COLLATEX_NS.equals(uri)) {
      return;
    }
    if ("collation".equals(name) && !witnesses.isEmpty()) {
      ignoreHooksCount++;
      final VariantGraph graph = new SimpleCollation(witnesses, algorithm, joined).collate(new JungVariantGraph());
      switch (format) {
        case TEI_APPARATUS:
          sendTeiApparatus(graph);
          break;
        default:
          sendAlignmentTable(graph);
          break;
      }
      ignoreHooksCount--;
    } else if ("witness".equals(name)) {
      witnesses.add(new SimpleWitness(sigil, endTextRecording()));
    }
  }

  private void sendAlignmentTable(VariantGraph graph) throws SAXException {
    startPrefixMapping("", COLLATEX_NS);
    startElement(COLLATEX_NS, "alignment", "alignment", EMPTY_ATTRIBUTES);
    final Set<Witness> witnesses = graph.witnesses();
    final RowSortedTable<Integer, Witness, Set<Token>> table = VariantGraphRanking.of(graph).asTable();

    for (Integer rowIndex : table.rowKeySet()) {
      final Map<Witness, Set<Token>> row = table.row(rowIndex);
      startElement(COLLATEX_NS, "row", "row", EMPTY_ATTRIBUTES);
      for (Witness witness : witnesses) {
        final AttributesImpl cellAttrs = new AttributesImpl();
        cellAttrs.addCDATAAttribute("sigil", witness.getSigil());
        startElement(COLLATEX_NS, "cell", "cell", cellAttrs);
        if (row.containsKey(witness)) {
          for (SimpleToken token : Ordering.natural().immutableSortedCopy(Iterables.filter(row.get(witness), SimpleToken.class))) {
            sendTextEvent(token.getContent());
          }
        }
        endElement(COLLATEX_NS, "cell", "cell");

      }
      endElement(COLLATEX_NS, "row", "row");
    }
    endElement(COLLATEX_NS, "alignment", "alignment");
    endPrefixMapping("");
  }

  private void sendTeiApparatus(VariantGraph graph) throws SAXException {
    try {
      ParallelSegmentationApparatus.generate(VariantGraphRanking.of(graph), new ParallelSegmentationApparatus.GeneratorCallback() {
        @Override
        public void start() {
          try {
            startPrefixMapping("cx", COLLATEX_NS);
            startPrefixMapping("", TEI_NS);
            startElement(COLLATEX_NS, "apparatus", "cx:apparatus", EMPTY_ATTRIBUTES);
          } catch (SAXException e) {
            throw Throwables.propagate(e);
          }
        }

        @Override
        public void segment(SortedMap<Witness, Iterable<Token>> contents) {
          final SetMultimap<String, Witness> segments = LinkedHashMultimap.create();
          for (Map.Entry<Witness, Iterable<Token>> cell : contents.entrySet()) {
            final StringBuilder sb = new StringBuilder();
            for (SimpleToken token : Ordering.natural().immutableSortedCopy(Iterables.filter(cell.getValue(), SimpleToken.class))) {
              sb.append(token.getContent());
            }
            segments.put(sb.toString(), cell.getKey());
          }

          final Set<String> segmentContents = segments.keySet();
          try {
            if (segmentContents.size() == 1) {
              sendTextEvent(Iterables.getOnlyElement(segmentContents));
            } else {
              startElement(TEI_NS, "app", "app", EMPTY_ATTRIBUTES);
              for (String segment : segmentContents) {
                final StringBuilder witnesses = new StringBuilder();
                for (Witness witness : segments.get(segment)) {
                  witnesses.append(witness.getSigil()).append(" ");
                }

                final AttributesImpl attributes = new AttributesImpl();
                attributes.addCDATAAttribute("wit", witnesses.toString().trim());
                startElement(TEI_NS, "rdg", "rdg", attributes);
                sendTextEvent(segment);
                endElement(TEI_NS, "rdg", "rdg");
              }
              endElement(TEI_NS, "app", "app");
            }
          } catch (SAXException e) {
            throw Throwables.propagate(e);
          }
        }

        @Override
        public void end() {
          try {
            endElement(COLLATEX_NS, "apparatus", "cx:apparatus");
            endPrefixMapping("");
            endPrefixMapping("cx");
          } catch (SAXException e) {
            throw Throwables.propagate(e);
          }
        }
      });
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), SAXException.class);
      throw Throwables.propagate(t);
    }
  }

  static String attributeValue(Attributes attr, String localName) {
    for (int ac = 0, al = attr.getLength(); ac < al; ac++) {
      if (localName.equals(attr.getLocalName(ac))) {
        return attr.getValue(ac);
      }
    }
    return null;
  }
}