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

package eu.interedition.collatex.tools;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.InspectableCollationAlgorithm;
import eu.interedition.collatex.matching.EditDistanceTokenComparator;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleCollation;
import eu.interedition.collatex.simple.SimplePatternTokenizer;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleTokenNormalizers;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.util.ParallelSegmentationApparatus;
import eu.interedition.collatex.util.VariantGraphRanking;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class JsonProcessor {

    public static SimpleCollation read(InputStream inputStream) throws IOException {
        try (JsonReader reader = Json.createReader(inputStream)) {
            final JsonStructure collationNode = reader.read();
            if (collationNode.getValueType() != JsonValue.ValueType.OBJECT) {
                throw new IOException("Expecting JSON object");
            }
            final JsonObject collationObject = (JsonObject) collationNode;

            JsonArray witnessesNode;
            try {
                witnessesNode = collationObject.getJsonArray("witnesses");
            } catch (ClassCastException e) {
                throw new IOException("Expecting 'witnesses' array");
            }

            final List<SimpleWitness> witnesses = new ArrayList<>(witnessesNode.size());

            for (JsonValue witnessNode : witnessesNode) {
                if (witnessNode.getValueType() != JsonValue.ValueType.OBJECT) {
                    throw new IOException("Expecting witness object");
                }
                final JsonObject witnessObject = (JsonObject) witnessNode;

                final String witnessId;
                try {
                    witnessId = witnessObject.getString("id").trim();
                } catch (ClassCastException e) {
                    throw new IOException("Expected textual witness 'id'");
                }
                if (witnessId.length() == 0) {
                    throw new IOException("Empty witness 'id' encountered");
                }

                final SimpleWitness witness = new SimpleWitness(witnessId);
                if (witnesses.contains(witness)) {
                    throw new IOException(String.format("Duplicate sigil for witness '%s", witness));
                }

                final JsonValue contentNode = witnessObject.get("content");
                final JsonValue tokensNode = witnessObject.get("tokens");
                if (contentNode == null && tokensNode == null) {
                    throw new IOException(String.format("Expected either 'tokens' or 'content' field in witness \"%s\"", witness));
                }

                if (tokensNode != null) {
                    if (tokensNode.getValueType() != JsonValue.ValueType.ARRAY) {
                        throw new IOException(String.format("Expected 'tokens' array in witness \"%s\"", witness));
                    }
                    final JsonArray tokensArray = (JsonArray) tokensNode;
                    final List<eu.interedition.collatex.Token> tokens = new ArrayList<>(tokensArray.size());
                    for (JsonValue tokenNode : tokensArray) {
                        if (tokenNode.getValueType() != JsonValue.ValueType.OBJECT) {
                            throw new IOException(String.format("Expected token object in 'tokens' field in witness \"%s\"", witness));
                        }
                        final JsonObject tokenObject = (JsonObject) tokenNode;
                        String tokenContent;
                        try {
                            tokenContent = tokenObject.getString("t");
                        } catch (ClassCastException | NullPointerException e) {
                            throw new IOException(String.format("Expected textual token content field 't' in witness \"%s\"", witness));
                        }

                        String normalizedTokenContent;
                        if (tokenObject.containsKey("n")) {
                            try {
                                normalizedTokenContent = tokenObject.getString("n");
                            } catch (ClassCastException e) {
                                throw new IOException(String.format("Expected textual normalized token content in witness \"%s\"", witness));
                            }
                        } else {
                            normalizedTokenContent = SimpleWitness.TOKEN_NORMALIZER.apply(tokenContent);
                        }

                        if (normalizedTokenContent == null || normalizedTokenContent.length() == 0) {
                            throw new IOException(String.format("Empty token encountered in witness \"%s\"", witness));
                        }

                        tokens.add(new Token(witness, tokenContent, normalizedTokenContent, tokenObject));
                    }
                    witness.setTokens(tokens);
                } else {
                    if (contentNode.getValueType() != JsonValue.ValueType.STRING) {
                        throw new IOException(String.format("Expected 'content' text field in witness \"%s\"", witness));
                    }
                    witness.setTokenContents(
                        SimplePatternTokenizer.BY_WS_OR_PUNCT.apply(((JsonString) contentNode).getString()),
                        SimpleTokenNormalizers.LC_TRIM_WS
                    );
                }
                witnesses.add(witness);
            }

            if (witnesses.isEmpty()) {
                throw new IOException("No witnesses in collation");
            }

            Comparator<eu.interedition.collatex.Token> tokenComparator = null;
            final JsonValue tokenComparatorNode = collationObject.get("tokenComparator");
            if (tokenComparatorNode != null && tokenComparatorNode.getValueType() == JsonValue.ValueType.OBJECT) {
                final JsonObject tokenComparatorObject = (JsonObject) tokenComparatorNode;
                try {
                    if ("levenshtein".equals(tokenComparatorObject.getString("type"))) {
                        final int configuredDistance = tokenComparatorObject.getInt("distance", 0);
                        tokenComparator = new EditDistanceTokenComparator(configuredDistance == 0 ? 1 : configuredDistance);
                    }
                } catch (ClassCastException e) {
                    // ignored
                }
            }
            if (tokenComparator == null) {
                tokenComparator = new EqualityTokenComparator();
            }

            CollationAlgorithm collationAlgorithm = null;
            final JsonValue collationAlgorithmNode = collationObject.get("algorithm");
            if (collationAlgorithmNode != null && collationAlgorithmNode.getValueType() == JsonValue.ValueType.STRING) {
                final String collationAlgorithmValue = ((JsonString) collationAlgorithmNode).getString();
                if ("needleman-wunsch".equalsIgnoreCase(collationAlgorithmValue)) {
                    collationAlgorithm = CollationAlgorithmFactory.needlemanWunsch(tokenComparator);
                } else if ("gst".equalsIgnoreCase(collationAlgorithmValue)) {
                    collationAlgorithm = CollationAlgorithmFactory.greedyStringTiling(tokenComparator, 2);
                } else if ("medite".equalsIgnoreCase(collationAlgorithmValue)) {
                    collationAlgorithm = CollationAlgorithmFactory.medite(tokenComparator, SimpleToken.TOKEN_MATCH_EVALUATOR);
                }
            }
            if (collationAlgorithm == null) {
                collationAlgorithm = CollationAlgorithmFactory.dekker(tokenComparator);
            }

            boolean joined = true;
            try {
                joined = collationObject.getBoolean("joined", true);
            } catch (ClassCastException e) {
                // ignored
            }

            if (collationAlgorithm instanceof InspectableCollationAlgorithm) {
                boolean mergeTranspositions = true;
                try {
                    mergeTranspositions = collationObject.getBoolean("transpositions", true);
                } catch (ClassCastException e) {
                    // ignored
                }
                ((InspectableCollationAlgorithm) collationAlgorithm).setMergeTranspositions(mergeTranspositions);
            }
            return new SimpleCollation(witnesses, collationAlgorithm, joined);
        }
    }

    public static void write(VariantGraph graph, OutputStream outputStream) throws IOException {
        try (final JsonGenerator jgen = Json.createGenerator(outputStream)) {
            write(jgen, graph);
        }
    }

    public static void write(VariantGraph graph, PrintWriter writer) throws IOException {
        try (final JsonGenerator jgen = Json.createGenerator(writer)) {
            write(jgen, graph);
        }
    }

    protected static void write(JsonGenerator jgen, VariantGraph graph) {
        ParallelSegmentationApparatus.generate(VariantGraphRanking.of(graph), new ParallelSegmentationApparatus.GeneratorCallback() {
            @Override
            public void start() {
                jgen.writeStartObject();

                jgen.writeStartArray("witnesses");
                graph.witnesses().stream().sorted(Witness.SIGIL_COMPARATOR).map(Witness::getSigil).forEach(jgen::write);
                jgen.writeEnd();


                jgen.writeStartArray("table");
            }

            @Override
            public void segment(SortedMap<Witness, Iterable<eu.interedition.collatex.Token>> contents) {
                jgen.writeStartArray();
                contents.values().forEach(tokens -> {
                    jgen.writeStartArray();
                    StreamSupport.stream(Spliterators.spliteratorUnknownSize(tokens.iterator(), Spliterator.NONNULL | Spliterator.IMMUTABLE), false)
                        .filter(t -> t instanceof SimpleToken)
                        .map(t -> (SimpleToken) t)
                        .sorted()
                        .forEach(t -> {
                            if (t instanceof Token) {
                                jgen.write(((Token) t).getJsonNode());
                            } else {
                                jgen.write(t.getContent());
                            }
                        });
                    jgen.writeEnd();
                });
                jgen.writeEnd();
            }

            @Override
            public void end() {
                jgen.writeEnd();
                jgen.writeEnd();
            }
        });
    }

    public static class Token extends SimpleToken {

        private final JsonObject jsonNode;

        public Token(SimpleWitness witness, String content, String normalized, JsonObject jsonNode) {
            super(witness, content, normalized);
            this.jsonNode = jsonNode;
        }

        public JsonObject getJsonNode() {
            return jsonNode;
        }
    }

    private JsonProcessor() {
    }
}
