/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright 2010-2012 The Interedition Development Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.interedition.collatex.dekker;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

public class DekkerAlgorithm extends CollationAlgorithm.Base {

  private final Comparator<Token> comparator;
  private final TokenLinker tokenLinker;
  private final PhraseMatchDetector phraseMatchDetector;
  private final TranspositionDetector transpositionDetector;
  private Map<Token, VariantGraphVertex> tokenLinks;
  private List<List<Match>> phraseMatches;
  private List<List<Match>> transpositions;
  private Map<Token, VariantGraphVertex> alignments;

  public DekkerAlgorithm(Comparator<Token> comparator) {
    this(comparator, new DefaultTokenLinker());
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

    if (LOG.isTraceEnabled()) {
      LOG.trace("{} + {}: {} vs. {}", new Object[] { graph, witness, graph.vertices(), tokens });
    }

    LOG.debug("{} + {}: Match and link tokens", graph, witness);
    tokenLinks = tokenLinker.link(graph, tokens, comparator);
    if (LOG.isTraceEnabled()) {
      for (Map.Entry<Token, VariantGraphVertex> tokenLink : tokenLinks.entrySet()) {
        LOG.trace("{} + {}: Token match: {} = {}", new Object[] { graph, witness, tokenLink.getValue(), tokenLink.getKey() });
      }
    }

    LOG.debug("{} + {}: Detect phrase matches", graph, witness);
    phraseMatches = phraseMatchDetector.detect(tokenLinks, graph, tokens);
    if (LOG.isTraceEnabled()) {
      for (List<Match> phraseMatch : phraseMatches) {
        LOG.trace("{} + {}: Phrase match: {}", new Object[] { graph, witness, Iterables.toString(phraseMatch) });
      }
    }

    LOG.debug("{} + {}: Detect transpositions", graph, witness);
    transpositions = transpositionDetector.detect(phraseMatches, graph);
    if (LOG.isTraceEnabled()) {
      for (List<Match> transposition : transpositions) {
        LOG.trace("{} + {}: Transposition: {}", new Object[] { graph, witness, Iterables.toString(transposition) });
      }
    }

    LOG.debug("{} + {}: Determine aligned tokens by filtering transpositions", graph, witness);
    alignments = Maps.newHashMap(tokenLinks);

    for (List<Match> transposedPhrase : transpositions) {
      for (Match match : transposedPhrase) {
        alignments.remove(match.token);
      }
    }
    if (LOG.isTraceEnabled()) {
      for (Map.Entry<Token, VariantGraphVertex> alignment : alignments.entrySet()) {
        LOG.trace("{} + {}: Alignment: {} = {}", new Object[] { graph, witness, alignment.getValue(), alignment.getKey() });
      }
    }

    final Map<Token, VariantGraphVertex> transposedTokens = Maps.newHashMap();
    for (List<Match> transposedPhrase : transpositions) {
      for (Match match : transposedPhrase) {
        transposedTokens.put(match.token, match.vertex);
      }
    }

    merge(graph, tokens, alignments, transposedTokens);

    if (LOG.isTraceEnabled()) {
      LOG.trace("!{}: {}", graph, Iterables.toString(graph.vertices()));
    }
  }

  public Map<Token, VariantGraphVertex> getTokenLinks() {
    return tokenLinks;
  }

  public List<List<Match>> getPhraseMatches() {
    return Collections.unmodifiableList(phraseMatches);
  }

  public List<List<Match>> getTranspositions() {
    return Collections.unmodifiableList(transpositions);
  }

  public Map<Token, VariantGraphVertex> getAlignments() {
    return Collections.unmodifiableMap(alignments);
  }
}
