/*
 * Copyright (c) 2015 The Interedition Development Group.
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

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.Tuple;
import eu.interedition.collatex.util.ParallelSegmentationApparatus;
import eu.interedition.collatex.util.StreamUtil;
import eu.interedition.collatex.util.VariantGraphRanking;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
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
    private final Map<VariantGraph.Vertex, Integer> vertexIds = new HashMap<>();
    private VariantGraphRanking ranking;

    public SimpleVariantGraphSerializer(VariantGraph graph) {
        this(graph, SIMPLE_TOKEN_TO_STRING);
    }

    public SimpleVariantGraphSerializer(VariantGraph graph, Function<Iterable<Token>, String> tokensToString) {
        this.graph = graph;
        this.tokensToString = tokensToString;
    }

    public void toTEI(final XMLStreamWriter xml) throws XMLStreamException {
        try {
            ParallelSegmentationApparatus.generate(ranking(), new ParallelSegmentationApparatus.GeneratorCallback() {
                @Override
                public void start() {
                    try {
                        xml.writeStartElement("cx", "apparatus", COLLATEX_NS);
                        xml.writeNamespace("cx", COLLATEX_NS);
                        xml.writeNamespace("", TEI_NS);
                    } catch (XMLStreamException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void segment(SortedMap<Witness, Iterable<Token>> contents) {
                    final Map<String, Set<Witness>> segments = new LinkedHashMap<>();
                    contents.forEach((witness, tokens) -> segments.computeIfAbsent(tokensToString.apply(tokens).trim(), k -> new HashSet<>()).add(witness));

                    final Set<String> segmentContents = segments.keySet();
                    try {
                        if (segmentContents.size() == 1) {
                            xml.writeCharacters(segmentContents.stream().findFirst().get());
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
                    } catch (XMLStreamException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void end() {
                    try {
                        xml.writeEndElement();
                    } catch (XMLStreamException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (RuntimeException re) {
            Throwable rootCause = re;
            for (Throwable cause = re; cause != null; cause = cause.getCause()) {
                rootCause = cause;
            }
            if (rootCause instanceof XMLStreamException) {
                throw (XMLStreamException) rootCause;
            }
            throw re;
        }
    }

    public void toCsv(final Writer out) throws IOException {
        try {
            ParallelSegmentationApparatus.generate(ranking(), new ParallelSegmentationApparatus.GeneratorCallback() {
                @Override
                public void start() {
                    try {
                        for (Iterator<Witness> it = graph.witnesses().stream().sorted(Witness.SIGIL_COMPARATOR).iterator(); it.hasNext(); ) {
                            out.write(escapeCsvField(it.next().getSigil()));
                            if (it.hasNext()) {
                                out.write(",");
                            }
                        }
                        out.write("\r\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void segment(SortedMap<Witness, Iterable<Token>> contents) {
                    try {
                        for (Iterator<Witness> witnessIt = contents.keySet().iterator(); witnessIt.hasNext(); ) {
                            out.write(escapeCsvField(tokensToString.apply(contents.getOrDefault(witnessIt.next(), Collections.emptySet()))));
                            if (witnessIt.hasNext()) {
                                out.write(",");
                            }
                        }
                        out.write("\r\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void end() {
                }
            });
        } catch (Throwable t) {
            for (Throwable cause = t; cause != null; cause = cause.getCause()) {
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
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

        for (VariantGraph.Vertex v : graph.vertices()) {
            for (Map.Entry<VariantGraph.Vertex, Set<Witness>> e : v.outgoing().entrySet()) {
                out.print(indent + id(v) + connector + id(e.getKey()));
                out.print(" [label = \"" + toDotLabel(e.getValue()) + "\"]");
                out.println(";");
            }
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
        return ("v" + numericId(vertex));
    }

    private int numericId(VariantGraph.Vertex vertex) {
        Integer id = vertexIds.computeIfAbsent(vertex, k -> vertexIds.size());
        return id;
    }

    String toDotLabel(Set<Witness> e) {
        return escapeDotLabel(e.stream().map(Witness::getSigil).distinct().sorted().collect(Collectors.joining(", ")));
    }

    String toDotLabel(VariantGraph.Vertex v) {
        return escapeDotLabel(vertexToString.apply(v));
    }

    static String escapeDotLabel(String string) {
        return string.replaceAll("\"", "\\\\\"").replaceAll("[\n\r]+", "\u00B6");
    }

    VariantGraphRanking ranking() {
        if (ranking == null) {
            ranking = VariantGraphRanking.of(graph);
        }
        return ranking;
    }

    Set<Tuple<VariantGraph.Vertex>> transposedTuples() {
        final Set<Tuple<VariantGraph.Vertex>> tuples = new HashSet<>();
        final Comparator<VariantGraph.Vertex> vertexOrdering = ranking().comparator();

        for (Set<VariantGraph.Vertex> transposition : graph.transpositions()) {
            final SortedMap<Witness, SortedSet<VariantGraph.Vertex>> verticesByWitness = new TreeMap<>(Witness.SIGIL_COMPARATOR);
            for (VariantGraph.Vertex vertex : transposition) {
                for (Witness witness : vertex.witnesses()) {
                    verticesByWitness.computeIfAbsent(witness, w -> new TreeSet<>(vertexOrdering)).add(vertex);
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
                            tuples.add(new Tuple<>(prevVertex, nextVertex));
                        }
                    }
                }
                prev = witness;
            }
        }

        return tuples;
    }

    public void toGraphML(XMLStreamWriter xml) throws XMLStreamException {
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

        final VariantGraphRanking ranking = ranking();
        for (VariantGraph.Vertex vertex : graph.vertices()) {
            final int id = numericId(vertex);
            xml.writeStartElement(GRAPHML_NS, NODE_TAG);
            xml.writeAttribute(ID_ATT, "n" + id);
            GraphMLProperty.NODE_NUMBER.write(Integer.toString(id), xml);
            GraphMLProperty.NODE_RANK.write(Integer.toString(ranking.apply(vertex)), xml);
            GraphMLProperty.NODE_TOKEN.write(vertexToString.apply(vertex), xml);
            xml.writeEndElement();
        }

        int edgeNumber = 0;
        for (VariantGraph.Vertex v : graph.vertices()) {
            for (Map.Entry<VariantGraph.Vertex, Set<Witness>> edge : v.outgoing().entrySet()) {
                xml.writeStartElement(GRAPHML_NS, EDGE_TAG);
                xml.writeAttribute(ID_ATT, "e" + edgeNumber);
                xml.writeAttribute(SOURCE_ATT, "n" + numericId(v));
                xml.writeAttribute(TARGET_ATT, "n" + numericId(edge.getKey()));
                GraphMLProperty.EDGE_NUMBER.write(Integer.toString(edgeNumber++), xml);
                GraphMLProperty.EDGE_TYPE.write(EDGE_TYPE_PATH, xml);
                GraphMLProperty.EDGE_WITNESSES.write(edge.getValue().stream().map(Witness::getSigil).distinct().sorted().collect(Collectors.joining(", ")), xml);
                xml.writeEndElement();
            }
        }

        for (Tuple<VariantGraph.Vertex> transposedTuple : transposedTuples()) {
            xml.writeStartElement(GRAPHML_NS, EDGE_TAG);
            xml.writeAttribute(ID_ATT, "e" + edgeNumber);
            xml.writeAttribute(SOURCE_ATT, "n" + numericId(transposedTuple.left));
            xml.writeAttribute(TARGET_ATT, "n" + numericId(transposedTuple.right));
            GraphMLProperty.EDGE_NUMBER.write(Integer.toString(edgeNumber++), xml);
            GraphMLProperty.EDGE_TYPE.write(EDGE_TYPE_TRANSPOSITION, xml);
            xml.writeEndElement();
        }

        xml.writeEndElement();

        xml.writeEndElement();
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
        NODE_RANK(NODE_TAG, "rank", "int"), //
        EDGE_NUMBER(EDGE_TAG, "number", "int"), //
        EDGE_TYPE(EDGE_TAG, "type", "string"), //
        EDGE_WITNESSES(EDGE_TAG, "witnesses", "string");

        private String name;
        private String forElement;
        private String type;

        GraphMLProperty(String forElement, String name, String type) {
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
        public String apply(VariantGraph.Vertex input) {
            return input.witnesses().stream().findFirst()
                    .map(witness -> tokensToString.apply(Arrays.asList(input.tokens().stream().filter(t -> witness.equals(t.getWitness())).toArray(Token[]::new))))
                    .orElse("");
        }
    };

    static final Function<Iterable<Token>, String> SIMPLE_TOKEN_TO_STRING = input -> StreamUtil.stream(input)
            .filter(t -> SimpleToken.class.isAssignableFrom(t.getClass()))
            .map(t -> (SimpleToken) t)
            .sorted()
            .map(SimpleToken::getContent)
            .collect(Collectors.joining());
}
