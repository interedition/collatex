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

package eu.interedition.collatex.io;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.matching.EditDistanceTokenComparator;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleCollation;
import eu.interedition.collatex.simple.SimplePatternTokenizer;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleTokenNormalizers;
import eu.interedition.collatex.simple.SimpleWitness;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
//TOODO: use the one without Jackson (Java 8)
public class SimpleCollationDeserializer extends JsonDeserializer<SimpleCollation> {

  @Override
  public SimpleCollation deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    final JsonNode collationNode = jp.readValueAsTree();

    final JsonNode witnessesNode = collationNode.path("witnesses");
    if (witnessesNode.isMissingNode() || !witnessesNode.isArray()) {
      throw JsonMappingException.from(jp, "Expecting 'witnesses' array");
    }

    final List<SimpleWitness> witnesses = new ArrayList<>();
    for (JsonNode witnessNode : witnessesNode) {
      if (!witnessNode.isObject()) {
        throw JsonMappingException.from(jp, "Expecting witness object");
      }
      final JsonNode witnessIdNode = witnessNode.path("id");
      if (witnessIdNode.isMissingNode() || !witnessIdNode.isTextual()) {
        throw JsonMappingException.from(jp, "Expected textual witness 'id'");
      }
      final String witnessIdStr = witnessIdNode.getTextValue().trim();
      if (witnessIdStr.length() == 0) {
        throw JsonMappingException.from(jp, "Empty witness 'id' encountered");
      }

      final SimpleWitness witness = new SimpleWitness(witnessIdStr);
      if (witnesses.contains(witness)) {
        throw JsonMappingException.from(jp, String.format("Duplicate sigil for witness '%s", witness));
      }

      final JsonNode contentNode = witnessNode.path("content");
      final JsonNode tokensNode = witnessNode.path("tokens");
      if (contentNode.isMissingNode() && tokensNode.isMissingNode()) {
        throw JsonMappingException.from(jp, String.format("Expected either 'tokens' or 'content' field in witness \"%s\"", witness));
      }

      if (!tokensNode.isMissingNode()) {
        if (!tokensNode.isArray()) {
          throw JsonMappingException.from(jp, String.format("Expected 'tokens' array in witness \"%s\"", witness));
        }
        List<Token> tokens = new ArrayList<>();
        for (JsonNode tokenNode : tokensNode) {
          if (!tokenNode.isObject()) {
            throw JsonMappingException.from(jp, String.format("Expected token object in 'tokens' field in witness \"%s\"", witness));
          }
          final JsonNode tokenContentNode = tokenNode.path("t");
          if (tokenContentNode.isMissingNode() || !tokenContentNode.isTextual()) {
            throw JsonMappingException.from(jp, String.format("Expected textual token content field 't' in witness \"%s\"", witness));
          }
          final String tokenContent = tokenContentNode.getTextValue();
          String normalizedTokenContent;
          final JsonNode normalizedTokenContentNode = tokenNode.path("n");
          if (normalizedTokenContentNode.isMissingNode()) {
            normalizedTokenContent = SimpleWitness.TOKEN_NORMALIZER.apply(tokenContent);
          } else {
            if (!normalizedTokenContentNode.isTextual()) {
              throw JsonMappingException.from(jp, String.format("Expected textual normalized token content in witness \"%s\"", witness));
            }
            normalizedTokenContent = normalizedTokenContentNode.getTextValue();
          }

          if (normalizedTokenContent.length() == 0) {
            throw JsonMappingException.from(jp, String.format("Empty token encountered in witness \"%s\"", witness));
          }

          tokens.add(new JsonToken(witness, tokenContent, normalizedTokenContent, tokenNode));
        }
        witness.setTokens(tokens);
      } else {
        if (!contentNode.isTextual()) {
          throw JsonMappingException.from(jp, String.format("Expected 'content' text field in witness \"%s\"", witness));
        }
        witness.setTokenContents(
                SimplePatternTokenizer.BY_WS_OR_PUNCT.apply(contentNode.getTextValue()),
                SimpleTokenNormalizers.LC_TRIM_WS
        );
      }
      witnesses.add(witness);
    }

    if (witnesses.isEmpty()) {
      throw JsonMappingException.from(jp, "No witnesses in collation");
    }

    Comparator<Token> tokenComparator = null;
    final JsonNode tokenComparatorNode = collationNode.path("tokenComparator");
    if (tokenComparatorNode.isObject()) {
      if ("levenshtein".equals(tokenComparatorNode.path("type").getTextValue())) {
        final int configuredDistance = tokenComparatorNode.path("distance").getIntValue();
        tokenComparator = new EditDistanceTokenComparator(configuredDistance == 0 ? 1 : configuredDistance);
      }
    }
    if (tokenComparator == null) {
      tokenComparator = new EqualityTokenComparator();
    }

    CollationAlgorithm collationAlgorithm = null;
    final JsonNode collationAlgorithmNode = collationNode.path("algorithm");
    if (collationAlgorithmNode.isTextual()) {
      final String collationAlgorithmValue = collationAlgorithmNode.getTextValue();
      if ("needleman-wunsch".equalsIgnoreCase(collationAlgorithmValue)) {
        collationAlgorithm = CollationAlgorithmFactory.needlemanWunsch(tokenComparator);
      } else if ("medite".equalsIgnoreCase(collationAlgorithmValue)) {
        collationAlgorithm = CollationAlgorithmFactory.medite(tokenComparator, SimpleToken.TOKEN_MATCH_EVALUATOR);
      }
    }
    if (collationAlgorithm == null) {
      collationAlgorithm = CollationAlgorithmFactory.dekker(tokenComparator);
    }

    boolean joined = true;
    final JsonNode joinedNode = collationNode.path("joined");
    if (joinedNode.isBoolean()) {
      joined = joinedNode.getBooleanValue();
    }

    boolean mergeTranspositions = true;
    final JsonNode transpositionsNode = collationNode.path("transpositions");
    if (transpositionsNode.isBoolean()) {
      mergeTranspositions = transpositionsNode.getBooleanValue();
    }
    if (collationAlgorithm instanceof DekkerAlgorithm) {
      ((DekkerAlgorithm) collationAlgorithm).setMergeTranspositions(mergeTranspositions);
    }
    return new SimpleCollation(witnesses, collationAlgorithm, joined);
  }
}
