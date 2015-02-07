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
package eu.interedition.collatex.dekker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.matrix.MatchTableLinker;
import eu.interedition.collatex.util.VariantGraphRanking;

public class DekkerAlgorithm extends CollationAlgorithm.Base {

  private final Comparator<Token> comparator;
  private final TokenLinker tokenLinker;
  private final PhraseMatchDetector phraseMatchDetector;
  private final TranspositionDetector transpositionDetector;
  private Map<Token, VariantGraph.Vertex> tokenLinks;
  private List<List<Match>> phraseMatches;
  private List<List<Match>> transpositions;
  private Map<Token, VariantGraph.Vertex> alignments;
  private boolean mergeTranspositions = false;
  
  public DekkerAlgorithm(Comparator<Token> comparator) {
    this(comparator, new MatchTableLinker());
  }

  public DekkerAlgorithm(Comparator<Token> comparator, TokenLinker tokenLinker) {
    this.comparator = comparator;
    this.tokenLinker = tokenLinker;
    this.phraseMatchDetector = new PhraseMatchDetector();
    this.transpositionDetector = new TranspositionDetector();
  }

  @Override
  public void collate(VariantGraph graph, Iterable<Token> tokens) {
    final Witness witness = StreamSupport.stream(tokens.spliterator(), false)
            .findFirst()
            .map(Token::getWitness)
            .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "{0} + {1}: {2} vs. {3}", new Object[] { graph, witness, graph.vertices(), tokens });
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "{0} + {1}: Match and link tokens", new Object[] { graph, witness });
    }
    tokenLinks = tokenLinker.link(graph, tokens, comparator);

    if (LOG.isLoggable(Level.FINER)) {
      for (Map.Entry<Token, VariantGraph.Vertex> tokenLink : tokenLinks.entrySet()) {
        LOG.log(Level.FINER, "{0} + {1}: Token match: {2} = {3}", new Object[] { graph, witness, tokenLink.getValue(), tokenLink.getKey() });
      }
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "{0} + {1}: Detect phrase matches", new Object[] { graph, witness });
    }
    phraseMatches = phraseMatchDetector.detect(tokenLinks, graph, tokens);
    if (LOG.isLoggable(Level.FINER)) {
      for (List<Match> phraseMatch : phraseMatches) {
        LOG.log(Level.FINER, "{0} + {1}: Phrase match: {2}", new Object[] { graph, witness, phraseMatch });
      }
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "{0} + {1}: Detect transpositions", new Object[] { graph, witness });
    }
    transpositions = transpositionDetector.detect(phraseMatches, graph);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "transpositions:{0}", transpositions);
    }

    if (LOG.isLoggable(Level.FINER)) {
      for (List<Match> transposition : transpositions) {
        LOG.log(Level.FINER, "{0} + {1}: Transposition: {2}", new Object[] { graph, witness, transposition });
      }
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "{0} + {1}: Determine aligned tokens by filtering transpositions", new Object[] { graph, witness });
    }
    alignments = new HashMap<>();
    for (List<Match> phrase : phraseMatches) {
      for (Match match : phrase) {
        alignments.put(match.token, match.vertex);
      }
    }

    for (List<Match> transposedPhrase : transpositions) {
      for (Match match : transposedPhrase) {
        alignments.remove(match.token);
      }
    }
    if (LOG.isLoggable(Level.FINER)) {
      for (Map.Entry<Token, VariantGraph.Vertex> alignment : alignments.entrySet()) {
        LOG.log(Level.FINER, "{0} + {1}: Alignment: {2} = {3}", new Object[] { graph, witness, alignment.getValue(), alignment.getKey() });
      }
    }

    merge(graph, tokens, alignments);

    // we filter out small transposed phrases over large distances
    List<List<Match>> falseTranspositions = new ArrayList<>();
    
    VariantGraphRanking ranking = VariantGraphRanking.of(graph);
    
    for (List<Match> transposedPhrase : transpositions) {
      Match match = transposedPhrase.get(0);
      VariantGraph.Vertex v1 = witnessTokenVertices.get(match.token);
      VariantGraph.Vertex v2 = match.vertex;
      int distance = Math.abs(ranking.apply(v1)-ranking.apply(v2))-1;
      if (distance > transposedPhrase.size()*3) {
        falseTranspositions.add(transposedPhrase);
      }
    }

    for (List<Match> transposition : falseTranspositions) {
      transpositions.remove(transposition);
    }

    if (mergeTranspositions) {
      mergeTranspositions(graph, transpositions);
    }
    
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "!{0}: {1}", new Object[] {graph, StreamSupport.stream(graph.vertices().spliterator(), false).map(Object::toString).collect(Collectors.joining(", ")) });
    }
  }

  public Map<Token, VariantGraph.Vertex> getTokenLinks() {
    return tokenLinks;
  }

  public List<List<Match>> getPhraseMatches() {
    return Collections.unmodifiableList(phraseMatches);
  }

  public List<List<Match>> getTranspositions() {
    return Collections.unmodifiableList(transpositions);
  }

  public Map<Token, VariantGraph.Vertex> getAlignments() {
    return Collections.unmodifiableMap(alignments);
  }

 /*
  * This check disables transposition rendering in the variant
  * graph when the variant graph contains more then two witnesses.
  * Transposition detection is done in a progressive manner
  * (witness by witness). When viewing the resulting graph
  * containing the variation for all witnesses
  * the detected transpositions can look strange, since segments
  * may have split into smaller or larger parts.
  */
  public void setMergeTranspositions(boolean b) {
    this.mergeTranspositions = b;
  }
}
