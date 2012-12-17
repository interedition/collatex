package eu.interedition.collatex.simple;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.neo4j.Neo4jVariantGraph;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphEdge;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;
import org.neo4j.graphdb.Transaction;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

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
  private final Map<VariantGraph.Vertex, Integer> vertexIds = Maps.newHashMap();

  public SimpleVariantGraphSerializer(VariantGraph graph) {
    this.graph = graph;
  }

  public void toTEI(XMLStreamWriter xml) throws XMLStreamException {
    final Set<Witness> allWitnesses = graph.witnesses();

    xml.writeStartDocument();
    xml.writeStartElement("cx", "apparatus", COLLATEX_NS);
    xml.writeNamespace("cx", COLLATEX_NS);
    xml.writeNamespace("", TEI_NS);

    for (Iterator<Set<VariantGraph.Vertex>> rowIt = graph.join().rank().adjustRanksForTranspositions().ranks().iterator(); rowIt.hasNext();) {
      final Set<VariantGraph.Vertex> row = rowIt.next();

      final SetMultimap<Witness, Token> tokenIndex = HashMultimap.create();
      for (VariantGraph.Vertex v : row) {
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
      GraphMLProperty.NODE_TOKEN.write(Neo4jVariantGraphVertex.TO_CONTENTS.apply(vertex), xml);
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
      GraphMLProperty.EDGE_WITNESSES.write(Neo4jVariantGraphEdge.TO_CONTENTS.apply(edge), xml);
      xml.writeEndElement();
    }
    for (VariantGraph.Transposition transposition : graph.transpositions()) {
      xml.writeStartElement(GRAPHML_NS, EDGE_TAG);
      xml.writeAttribute(ID_ATT, "e" + edgeNumber);
      xml.writeAttribute(SOURCE_ATT, vertexToId.get(transposition.from()));
      xml.writeAttribute(TARGET_ATT, vertexToId.get(transposition.to()));
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

  public void toDot(VariantGraph graph, Writer writer) {
      final PrintWriter out = new PrintWriter(writer);
      final String indent = "  ";
      final String connector = " -> ";

      out.println("digraph G {");

      for (VariantGraph.Vertex v : graph.vertices()) {
        out.print(indent + id(v));
        out.print(" [label = \"" + toLabel(v) + "\"]");
        out.println(";");
      }

      for (VariantGraph.Edge e : graph.edges()) {
        out.print(indent + id(e.from()) + connector + id(e.to()));
        out.print(" [label = \"" + toLabel(e) + "\"]");
        out.println(";");
      }

      for (VariantGraph.Transposition t : graph.transpositions()) {
        out.print(indent + id(t.from()) + connector + id(t.to()));
        out.print(" [label = \"" + t.getId() + "\", color = \"lightgray\", style = \"dashed\" arrowhead = \"none\", arrowtail = \"none\" ]");
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

  private String toLabel(VariantGraph.Edge e) {
    return escapeLabel(Neo4jVariantGraphEdge.TO_CONTENTS.apply(e));
  }

  private String toLabel(VariantGraph.Vertex v) {
    return escapeLabel(Neo4jVariantGraphVertex.TO_CONTENTS.apply(v));
  }

  String escapeLabel(String string) {
    return string.replaceAll("\"", "\\\"").replaceAll("\n", "[LB]");
  }
}
