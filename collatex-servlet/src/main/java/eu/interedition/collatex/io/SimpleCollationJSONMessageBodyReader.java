package eu.interedition.collatex.io;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.InspectableCollationAlgorithm;
import eu.interedition.collatex.matching.EditDistanceRatioTokenComparator;
import eu.interedition.collatex.matching.EditDistanceTokenComparator;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.*;

import javax.json.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Created by ronald on 5/5/15.
 * Based on code written by Gregor Middell.
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class SimpleCollationJSONMessageBodyReader implements MessageBodyReader<SimpleCollation> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return SimpleCollation.class.isAssignableFrom(type);
    }

    @Override
    public SimpleCollation readFrom(Class<SimpleCollation> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try (JsonReader reader = Json.createReader(entityStream)) {
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

                        tokens.add(new VariantGraphJSONMessageBodyWriter.Token(witness, tokenContent, normalizedTokenContent, tokenObject));
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

            CollationAlgorithm collationAlgorithm = createFromJSON(collationObject);

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

    /**
     * Create CollationAlgorithm from a JSON snippet
     *
     * This method is duplicated in {@code JsonProcessor}.
     *
     * FIXME: This method could be moved into {@code CollationAlgorithmFactory}
     * but it would make collatex-core dependent on javax.json.
     *
     * @param collationObject The JSON snippet
     * @return                The CollationAlgorithm subclass
     */
    private static CollationAlgorithm createFromJSON(JsonObject collationObject) {
        Comparator<Token> comparator = null;

        final JsonValue tokenComparatorNode = collationObject.get("tokenComparator");
        if (tokenComparatorNode != null && tokenComparatorNode.getValueType() == JsonValue.ValueType.OBJECT) {
            final JsonObject tokenComparatorObject = (JsonObject) tokenComparatorNode;
            try {
                if ("levenshtein".equals(tokenComparatorObject.getString("type"))) {
                    if (tokenComparatorObject.containsKey("ratio")) {
                        comparator = CollationAlgorithmFactory.createComparator (
                            "levenshtein.ratio",
                            new Double (tokenComparatorObject.getJsonNumber("ratio").doubleValue()));
                    } else {
                        comparator = CollationAlgorithmFactory.createComparator (
                            "levenshtein.distance",
                            new Integer (tokenComparatorObject.getInt("distance", 1)));
                    }
                }
            } catch (ClassCastException e) {
                // ignored
            }
        }
        if (comparator == null) {
            comparator = CollationAlgorithmFactory.createComparator ("equality");
        }

        String algorithm = "dekker";
        final JsonValue collationAlgorithmNode = collationObject.get("algorithm");
        if (collationAlgorithmNode != null &&
            collationAlgorithmNode.getValueType() == JsonValue.ValueType.STRING) {
            algorithm = ((JsonString) collationAlgorithmNode).getString();
        }

        return CollationAlgorithmFactory.createAlgorithm(algorithm, comparator);
    }

}
