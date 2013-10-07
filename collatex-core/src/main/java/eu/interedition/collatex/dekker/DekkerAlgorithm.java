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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.matrix.MatchTableLinker;

public class DekkerAlgorithm extends CollationAlgorithm.Base {

  private final Comparator<Token> comparator;
  private final TokenLinker tokenLinker;
  private final PhraseMatchDetector phraseMatchDetector;
  private final TranspositionDetector transpositionDetector;
  private Map<Token, VariantGraph.Vertex> tokenLinks;
  private List<List<Match>> phraseMatches;
  private List<List<Match>> transpositions;
  private Map<Token, VariantGraph.Vertex> alignments;

  public DekkerAlgorithm(Comparator<Token> comparator) {
    this(comparator, new MatchTableLinker(3));
  }

  public DekkerAlgorithm(Comparator<Token> comparator, TokenLinker tokenLinker) {
    this.comparator = comparator;
    this.tokenLinker = tokenLinker;
    this.phraseMatchDetector = new PhraseMatchDetector();
    this.transpositionDetector = new TranspositionDetector();
  }

  @Override
  public void collate(VariantGraph graph, Iterable<Token> tokens) {
    Preconditions.checkArgument(!Iterables.isEmpty(tokens), "Empty witness");
    final Witness witness = Iterables.getFirst(tokens, null).getWitness();

    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "{0} + {1}: {2} vs. {3}", new Object[] { graph, witness, graph.vertices(), tokens });
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "{0} + {1}: Match and link tokens", new Object[] { graph, witness });
    }
    tokenLinks = tokenLinker.link(graph, tokens, comparator);
    //    new SimpleVariantGraphSerializer(graph).toDot(graph, writer);
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
        LOG.log(Level.FINER, "{0} + {1}: Phrase match: {2}", new Object[] { graph, witness, Iterables.toString(phraseMatch) });
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
        LOG.log(Level.FINER, "{0} + {1}: Transposition: {2}", new Object[] { graph, witness, Iterables.toString(transposition) });
      }
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "{0} + {1}: Determine aligned tokens by filtering transpositions", new Object[] { graph, witness });
    }
    alignments = Maps.newHashMap();
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
    mergeTranspositions(graph, transpositions);

    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "!{0}: {1}", new Object[] {graph, Iterables.toString(graph.vertices())});
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

}
