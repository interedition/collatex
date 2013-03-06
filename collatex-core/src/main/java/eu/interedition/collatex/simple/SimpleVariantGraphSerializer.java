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

package eu.interedition.collatex.simple;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.Tuple;
import eu.interedition.collatex.util.VariantGraphRanking;

import javax.annotation.Nullable;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleVariantGraphSerializer {
  /**
   * CollateX custom namespace.
   */
  protected static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";

  /**
   * The TEI P5 namespace.
   */
  protected static final String TEI_NS = "http://www.tei-c.org/ns/1.0";

  private final VariantGraph graph;
  private final Function<Iterable<Token>, String> tokensToString;
  private final Map<VariantGraph.Vertex, Integer> vertexIds = Maps.newHashMap();
  private final Map<VariantGraph.Transposition, Integer> transpositionIds = Maps.newHashMap();
  private VariantGraphRanking ranking;

  public SimpleVariantGraphSerializer(VariantGraph graph) {
    this(graph, SIMPLE_TOKEN_TO_STRING);
  }

  public SimpleVariantGraphSerializer(VariantGraph graph, Function<Iterable<Token>, String> tokensToString) {
    this.graph = graph;
    this.tokensToString = tokensToString;
  }

  public void toTEI(Writer out) throws IOException {
    try {
      XMLStreamWriter xml = null;
      try {
        toTEI(xml = XMLOutputFactory.newInstance().createXMLStreamWriter(out));
      } finally {
        if (xml != null) {
          xml.close();
        }
      }
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  public void toTEI(XMLStreamWriter xml) throws XMLStreamException {
    xml.writeStartDocument();
    xml.writeStartElement("cx", "apparatus", COLLATEX_NS);
    xml.writeNamespace("cx", COLLATEX_NS);
    xml.writeNamespace("", TEI_NS);

    final VariantGraphRanking ranking = ranking();
    final Set<Witness> allWitnesses = graph.witnesses();
    for (Iterator<Map.Entry<Integer, Collection<VariantGraph.Vertex>>> rowIt = ranking.getByRank().asMap().entrySet().iterator(); rowIt.hasNext(); ) {
      final Map.Entry<Integer, Collection<VariantGraph.Vertex>> row = rowIt.next();
      final int rank = row.getKey();
      final Collection<VariantGraph.Vertex> vertices = row.getValue();

      if (vertices.size() == 1 && Iterables.getOnlyElement(vertices).tokens().isEmpty()) {
        // skip start and end vertex
        continue;
      }

      // spreading vertices with same rank according to their registered transpositions
      final Multimap<Integer, VariantGraph.Vertex> verticesByTranspositionRank = HashMultimap.create();
      for (VariantGraph.Vertex v : vertices) {
        int transpositionRank = 0;
        for (VariantGraph.Transposition transposition : v.transpositions()) {
          for (VariantGraph.Vertex tv : transposition) {
            transpositionRank += (ranking.apply(tv).intValue() - rank);
          }
        }
        verticesByTranspositionRank.put(transpositionRank, v);
      }

      // render segments
      for (Iterator<Integer> transpositionRankIt = Ordering.natural().immutableSortedCopy(verticesByTranspositionRank.keySet()).iterator(); transpositionRankIt.hasNext() ;) {
        final Multimap<Witness, Token> tokensByWitness = HashMultimap.create();
        for (VariantGraph.Vertex v : verticesByTranspositionRank.get(transpositionRankIt.next())) {
          for (Token token : v.tokens()) {
            tokensByWitness.put(token.getWitness(), token);
          }
        }
        final SortedMap<Witness, String> cellContents = Maps.newTreeMap(Witness.SIGIL_COMPARATOR);
        for (Witness witness : allWitnesses) {
          cellContents.put(witness, tokensByWitness.containsKey(witness) ? tokensToString.apply(tokensByWitness.get(witness)) : "");
        }

        final SetMultimap<String, Witness> segments = LinkedHashMultimap.create();
        for (Map.Entry<Witness, String> cell : cellContents.entrySet()) {
          segments.put(cell.getValue(), cell.getKey());
        }

        final Set<String> segmentContents = segments.keySet();
        if (segmentContents.size() == 1) {
          xml.writeCharacters(Iterables.getOnlyElement(segmentContents));
        } else {
          xml.writeStartElement("", "app", TEI_NS);
          for (String segment : segmentContents) {
            final StringBuilder witnesses = new StringBuilder();
            for (Witness witness : segments.get(segment)) {
              witnesses.append(witness.getSigil()).append(" ");
            }
            if (segment.length() == 0) {
              xml.writeEmptyElement("", "rdg", TEI_NS);
            } else {
              xml.writeStartElement("", "rdg", TEI_NS);
            }

            xml.writeAttribute("wit", witnesses.toString().trim());

            if (segment.length() > 0) {
              xml.writeCharacters(segment);
              xml.writeEndElement();
            }
          }
          xml.writeEndElement();
        }
      }
    }

    xml.writeEndElement();
    xml.writeEndDocument();
  }

  public void toCsv(Writer out) throws IOException {
    final List<Witness> witnessList = Ordering.from(Witness.SIGIL_COMPARATOR).immutableSortedCopy(graph.witnesses());
    for (Iterator<Witness> it = witnessList.iterator(); it.hasNext(); ) {
      out.write(escapeCsvField(it.next().getSigil()));
      if (it.hasNext()) {
        out.write(",");
      }
    }
    out.write("\r\n");

    final RowSortedTable<Integer,Witness,Set<Token>> table = ranking().asTable();
    for (Iterator<Integer> rowIt = table.rowKeySet().iterator(); rowIt.hasNext(); ) {
      final Map<Witness,Set<Token>> rowContents = table.row(rowIt.next());
      for (Iterator<Witness> witnessIt = witnessList.iterator(); witnessIt.hasNext();) {
        out.write(escapeCsvField(tokensToString.apply(Objects.firstNonNull(rowContents.get(witnessIt.next()), Collections.<Token>emptySet()))));
        if (witnessIt.hasNext()) {
          out.write(",");
        }
      }
      if (rowIt.hasNext()) {
        out.write("\r\n");
      }

    }

  }

  static final Pattern CSV_SPECIAL_CHARS = Pattern.compile("[\r\n\",]");

  static String escapeCsvField(String content) {
    return (CSV_SPECIAL_CHARS.matcher(content).find() ? ("\"" + content.replaceAll("\"", "\"\"") + "\"") : content);
  }

  public void toDot(Writer writer) {
      final PrintWriter out = new PrintWriter(writer);
      final String indent = "  ";
      final String connector = " -> ";

      out.println("digraph G {");

      for (VariantGraph.Vertex v : graph.vertices()) {
        out.print(indent + id(v));
        out.print(" [label = \"" + toDotLabel(v) + "\"]");
        out.println(";");
      }

      for (VariantGraph.Edge e : graph.edges()) {
        out.print(indent + id(e.from()) + connector + id(e.to()));
        out.print(" [label = \"" + toDotLabel(e) + "\"]");
        out.println(";");
      }

      for (Tuple<VariantGraph.Vertex> transposedTuple : transposedTuples()) {
        final String leftId = id(transposedTuple.left);
        final String rightId = id(transposedTuple.right);
        out.print(indent + leftId + connector + rightId);
        out.print(" [ color = \"lightgray\", style = \"dashed\" arrowhead = \"none\", arrowtail = \"none\" ]");
        out.println(";");
      }

      out.print(indent + id(graph.getStart()) + connector + id(graph.getEnd()));
      out.print(" [color =  \"white\"]");
      out.println(";");

      out.println("}");

      out.flush();
  }

  private String id(VariantGraph.Vertex vertex) {
    Integer id = vertexIds.get(vertex);
    if (id == null) {
      id = vertexIds.size();
      vertexIds.put(vertex, id);
    }
    return ("v" + id);
  }

  private String id(VariantGraph.Transposition transposition) {
    Integer id = transpositionIds.get(transposition);
    if (id == null) {
      id = transpositionIds.size();
      transpositionIds.put(transposition, id);
    }
    return ("t" + id);
  }

  String toDotLabel(VariantGraph.Edge e) {
    return escapeDotLabel(Witness.TO_SIGILS.apply(e));
  }

  String toDotLabel(VariantGraph.Vertex v) {
    return escapeDotLabel(vertexToString.apply(v));
  }

  static String escapeDotLabel(String string) {
    return string.replaceAll("\"", "\\\"").replaceAll("[\n\r]+", "\u00B6");
  }

  VariantGraphRanking ranking() {
    if (ranking == null) {
      ranking = VariantGraphRanking.of(graph);
    }
    return ranking;
  }

  Set<Tuple<VariantGraph.Vertex>> transposedTuples() {
    final Set<Tuple<VariantGraph.Vertex>> tuples = Sets.newHashSet();
    final Ordering<VariantGraph.Vertex> vertexOrdering = Ordering.from(ranking()).compound(new Comparator<VariantGraph.Vertex>() {
      @Override
      public int compare(VariantGraph.Vertex o1, VariantGraph.Vertex o2) {
        return Ordering.arbitrary().compare(o1, o2);
      }
    });

    for (VariantGraph.Transposition transposition : graph.transpositions()) {
      final SortedSetMultimap<Witness, VariantGraph.Vertex> verticesByWitness = TreeMultimap.create(Witness.SIGIL_COMPARATOR, vertexOrdering);
      for (VariantGraph.Vertex vertex : transposition) {
        for (Witness witness : vertex.witnesses()) {
          verticesByWitness.put(witness, vertex);
        }
      }

      Witness prev = null;
      for (Witness witness : verticesByWitness.keySet()) {
        if (prev != null) {
          final Iterator<VariantGraph.Vertex> prevIt = verticesByWitness.get(prev).iterator();
          final Iterator<VariantGraph.Vertex> nextIt = verticesByWitness.get(witness).iterator();
          while (prevIt.hasNext() && nextIt.hasNext()) {
            final VariantGraph.Vertex prevVertex = prevIt.next();
            final VariantGraph.Vertex nextVertex = nextIt.next();
            if (!prevVertex.equals(nextVertex)) {
              tuples.add(new Tuple<VariantGraph.Vertex>(prevVertex, nextVertex));
            }
          }
        }
        prev = witness;
      }
    }

    return tuples;
  }

  public void toGraphML(Writer out) throws IOException {
    try {
      XMLStreamWriter xml = null;
      try {
        toGraphML(xml = XMLOutputFactory.newInstance().createXMLStreamWriter(out));
      } finally {
        if (xml != null) {
          xml.close();
        }
      }
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  public void toGraphML(XMLStreamWriter xml) throws XMLStreamException {
    xml.writeStartDocument();
    xml.writeStartElement("", GRAPHML_TAG, GRAPHML_NS);
    xml.writeNamespace("", GRAPHML_NS);
    xml.writeAttribute(XMLNSXSI_ATT, GRAPHML_XMLNSXSI);
    xml.writeAttribute(XSISL_ATT, GRAPHML_XSISL);

    for (GraphMLProperty p : GraphMLProperty.values()) {
      p.declare(xml);
    }

    xml.writeStartElement(GRAPHML_NS, GRAPH_TAG);
    xml.writeAttribute(ID_ATT, GRAPH_ID);
    xml.writeAttribute(EDGEDEFAULT_ATT, EDGEDEFAULT_DEFAULT_VALUE);
    xml.writeAttribute(PARSENODEIDS_ATT, PARSENODEIDS_DEFAULT_VALUE);
    xml.writeAttribute(PARSEEDGEIDS_ATT, PARSEEDGEIDS_DEFAULT_VALUE);
    xml.writeAttribute(PARSEORDER_ATT, PARSEORDER_DEFAULT_VALUE);

    final Map<VariantGraph.Vertex, String> vertexToId = Maps.newHashMap();
    int vertexNumber = 0;
    for (VariantGraph.Vertex vertex : graph.vertices()) {
      final String vertexNodeID = "n" + vertexNumber;
      xml.writeStartElement(GRAPHML_NS, NODE_TAG);
      xml.writeAttribute(ID_ATT, vertexNodeID);
      GraphMLProperty.NODE_NUMBER.write(Integer.toString(vertexNumber++), xml);
      GraphMLProperty.NODE_TOKEN.write(vertexToString.apply(vertex), xml);
      xml.writeEndElement();
      vertexToId.put(vertex, vertexNodeID);
    }

    int edgeNumber = 0;
    for (VariantGraph.Edge edge : graph.edges()) {
      xml.writeStartElement(GRAPHML_NS, EDGE_TAG);
      xml.writeAttribute(ID_ATT, "e" + edgeNumber);
      xml.writeAttribute(SOURCE_ATT, vertexToId.get(edge.from()));
      xml.writeAttribute(TARGET_ATT, vertexToId.get(edge.to()));
      GraphMLProperty.EDGE_NUMBER.write(Integer.toString(edgeNumber++), xml);
      GraphMLProperty.EDGE_TYPE.write(EDGE_TYPE_PATH, xml);
      GraphMLProperty.EDGE_WITNESSES.write(Witness.TO_SIGILS.apply(edge), xml);
      xml.writeEndElement();
    }

    for (Tuple<VariantGraph.Vertex> transposedTuple : transposedTuples()) {
      xml.writeStartElement(GRAPHML_NS, EDGE_TAG);
      xml.writeAttribute(ID_ATT, "e" + edgeNumber);
      xml.writeAttribute(SOURCE_ATT, vertexToId.get(transposedTuple.left));
      xml.writeAttribute(TARGET_ATT, vertexToId.get(transposedTuple.right));
      GraphMLProperty.EDGE_NUMBER.write(Integer.toString(edgeNumber++), xml);
      GraphMLProperty.EDGE_TYPE.write(EDGE_TYPE_TRANSPOSITION, xml);
      xml.writeEndElement();
    }

    xml.writeEndElement();

    xml.writeEndElement();
    xml.writeEndDocument();
  }

  private static final String NODE_TAG = "node";
  private static final String TARGET_ATT = "target";
  private static final String SOURCE_ATT = "source";
  private static final String EDGE_TAG = "edge";
  private static final String EDGE_TYPE_PATH = "path";
  private static final String EDGE_TYPE_TRANSPOSITION = "transposition";
  private static final String EDGEDEFAULT_DEFAULT_VALUE = "directed";
  private static final String EDGEDEFAULT_ATT = "edgedefault";
  private static final String GRAPH_ID = "g0";
  private static final String GRAPH_TAG = "graph";
  private static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
  private static final String GRAPHML_TAG = "graphml";
  private static final String XMLNSXSI_ATT = "xmlns:xsi";
  private static final String XSISL_ATT = "xsi:schemaLocation";
  private static final String GRAPHML_XMLNSXSI = "http://www.w3.org/2001/XMLSchema-instance";
  private static final String GRAPHML_XSISL = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
  private static final String PARSENODEIDS_ATT = "parse.nodeids";
  private static final String PARSENODEIDS_DEFAULT_VALUE = "canonical";
  private static final String PARSEEDGEIDS_ATT = "parse.edgeids";
  private static final String PARSEEDGEIDS_DEFAULT_VALUE = "canonical";
  private static final String PARSEORDER_ATT = "parse.order";
  private static final String PARSEORDER_DEFAULT_VALUE = "nodesfirst";

  private static final String ATTR_TYPE_ATT = "attr.type";
  private static final String ATTR_NAME_ATT = "attr.name";
  private static final String FOR_ATT = "for";
  private static final String ID_ATT = "id";
  private static final String KEY_TAG = "key";
  private static final String DATA_TAG = "data";

  private enum GraphMLProperty {
    NODE_NUMBER(NODE_TAG, "number", "int"), //
    NODE_TOKEN(NODE_TAG, "tokens", "string"), //
    EDGE_NUMBER(EDGE_TAG, "number", "int"), //
    EDGE_TYPE(EDGE_TAG, "type", "string"), //
    EDGE_WITNESSES(EDGE_TAG, "witnesses", "string");

    private String name;
    private String forElement;
    private String type;

    private GraphMLProperty(String forElement, String name, String type) {
      this.name = name;
      this.forElement = forElement;
      this.type = type;
    }

    public void write(String data, XMLStreamWriter xml) throws XMLStreamException {
      xml.writeStartElement(GRAPHML_NS, DATA_TAG);
      xml.writeAttribute(KEY_TAG, "d" + ordinal());
      xml.writeCharacters(data);
      xml.writeEndElement();
    }

    public void declare(XMLStreamWriter xml) throws XMLStreamException {
      xml.writeEmptyElement(GRAPHML_NS, KEY_TAG);
      xml.writeAttribute(ID_ATT, "d" + ordinal());
      xml.writeAttribute(FOR_ATT, forElement);
      xml.writeAttribute(ATTR_NAME_ATT, name);
      xml.writeAttribute(ATTR_TYPE_ATT, type);
    }
  }

  final Function<VariantGraph.Vertex, String> vertexToString = new Function<VariantGraph.Vertex, String>() {
    @Override
    public String apply(@Nullable VariantGraph.Vertex input) {
      final Witness witness = Iterables.getFirst(input.witnesses(), null);
      return (witness == null ? "" : tokensToString.apply(input.tokens(Collections.singleton(witness))));
    }
  };

  static final Function<Iterable<Token>, String> SIMPLE_TOKEN_TO_STRING = new Function<Iterable<Token>, String>() {
    public String apply(@Nullable Iterable<Token> input) {
      final List<SimpleToken> tokens = Ordering.natural().immutableSortedCopy(
              Iterables.filter(input, SimpleToken.class)
      );
      final StringBuilder sb = new StringBuilder();
      for (SimpleToken token : tokens) {
        sb.append(token.getContent());
      }
      return sb.toString();
    }
  };
}
