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

package eu.interedition.collatex;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschScorer;
import eu.interedition.collatex.util.VariantGraphRanking;
import eu.interedition.collatex.util.VertexMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface CollationAlgorithm {

  void collate(VariantGraph against, Iterable<Token> witness);

  void collate(VariantGraph against, Iterable<Token>... witnesses);

  void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses);

  abstract class Base implements CollationAlgorithm {
    protected final Logger LOG = Logger.getLogger(getClass().getName());
    protected Map<Token, VariantGraph.Vertex> witnessTokenVertices;

    @Override
    public void collate(VariantGraph against, Iterable<Token>... witnesses) {
      collate(against, Arrays.asList(witnesses));
    }

    @Override
    public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
      for (Iterable<Token> witness : witnesses) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.log(Level.FINE, "heap space: {0}/{1}", new Object[] {
                  Runtime.getRuntime().totalMemory(),
                  Runtime.getRuntime().maxMemory()
          });
        }
        collate(against, witness);
      }
    }

    protected void merge(VariantGraph into, Iterable<Token> witnessTokens, Map<Token, VariantGraph.Vertex> alignments) {
      Preconditions.checkArgument(!Iterables.isEmpty(witnessTokens), "Empty witness");
      final Witness witness = Iterables.getFirst(witnessTokens, null).getWitness();

      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "{0} + {1}: Merge comparand into graph", new Object[] { into, witness });
      }
      witnessTokenVertices = Maps.newHashMap();
      VariantGraph.Vertex last = into.getStart();
      final Set<Witness> witnessSet = Collections.singleton(witness);
      for (Token token : witnessTokens) {
        VariantGraph.Vertex matchingVertex = alignments.get(token);
        if (matchingVertex == null) {
          matchingVertex = into.add(token);
        } else {
          if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Match: {0} to {1}", new Object[] { matchingVertex, token });
          }
          matchingVertex.add(Collections.singleton(token));
        }
        witnessTokenVertices.put(token, matchingVertex);

        into.connect(last, matchingVertex, witnessSet);
        last = matchingVertex;
      }
      into.connect(last, into.getEnd(), witnessSet);
    }

    protected void mergeTranspositions(VariantGraph into, Iterable<SortedSet<VertexMatch.WithToken>> transpositions) {
      for (SortedSet<VertexMatch.WithToken> transposedPhrase : transpositions) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.log(Level.FINE, "Transposition: {0}", transposedPhrase);
        }
        final Set<VariantGraph.Vertex> transposed = Sets.newHashSet();
        for (VertexMatch.WithToken match : transposedPhrase) {
          transposed.add(witnessTokenVertices.get(match.token));
          transposed.add(match.vertex);
        }
        into.transpose(transposed);
      }
    }

    protected void mergeTranspositions(VariantGraph into, List<List<Match>> transpositions) {
      for (List<Match> transposedPhrase : transpositions) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.log(Level.FINE, "Transposition: {0}", transposedPhrase);
        }
        final Set<VariantGraph.Vertex> transposed = Sets.newHashSet();
        for (Match match : transposedPhrase) {
          transposed.add(witnessTokenVertices.get(match.token));
          transposed.add(match.vertex);
        }
        into.transpose(transposed);
      }
    }

    protected void merge(VariantGraph graph, VariantGraphRanking ranking, Token[] tokens, SortedSet<SortedSet<VertexMatch.WithTokenIndex>> matches) {
      final SortedSet<VertexMatch.WithTokenIndex>[] matchesVertexOrder = (SortedSet<VertexMatch.WithTokenIndex>[]) matches.toArray(new SortedSet[matches.size()]);
      final SortedSet<VertexMatch.WithTokenIndex>[] matchesTokenOrder = Arrays.copyOf(matchesVertexOrder, matchesVertexOrder.length);

      Arrays.sort(matchesTokenOrder, new Comparator<SortedSet<VertexMatch.WithTokenIndex>>() {
        @Override
        public int compare(SortedSet<VertexMatch.WithTokenIndex> o1, SortedSet<VertexMatch.WithTokenIndex> o2) {
          return (o1.first().token - o2.first().token);
        }
      });

      final int mergedLength = Math.max(tokens.length, ranking.size());
      final Set<SortedSet<VertexMatch.WithTokenIndex>> inOrderMatches = NeedlemanWunschAlgorithm.align(
              matchesVertexOrder,
              matchesTokenOrder,
              new NeedlemanWunschScorer<SortedSet<VertexMatch.WithTokenIndex>, SortedSet<VertexMatch.WithTokenIndex>>() {

                @Override
                public float score(SortedSet<VertexMatch.WithTokenIndex> a, SortedSet<VertexMatch.WithTokenIndex> b) {
                  return (a.equals(b) ? 1 : -mergedLength);
                }

                @Override
                public float gap() {
                  return -(1 / (mergedLength * 1.0f));
                }
              }
      ).keySet();

      final List<SortedSet<VertexMatch.WithTokenIndex>> transpositions = new ArrayList<SortedSet<VertexMatch.WithTokenIndex>>();
      for (SortedSet<VertexMatch.WithTokenIndex> phraseMatch : matches) {
        if (!inOrderMatches.contains(phraseMatch)) {
          transpositions.add(phraseMatch);
        }
      }


      final Map<Token, VariantGraph.Vertex> matchedTokens = Maps.newHashMap();
      for (SortedSet<VertexMatch.WithTokenIndex> phraseMatch : matches) {
        for (VertexMatch.WithTokenIndex tokenMatch : phraseMatch) {
          matchedTokens.put(tokens[tokenMatch.token], tokenMatch.vertex);
        }
      }

      final List<SortedSet<VertexMatch.WithToken>> transposedTokens = Lists.newLinkedList();
      for (SortedSet<VertexMatch.WithTokenIndex> transposition : transpositions) {
        final SortedSet<VertexMatch.WithToken> transpositionMatch = new TreeSet<VertexMatch.WithToken>();
        for (VertexMatch.WithTokenIndex match : transposition) {
          matchedTokens.remove(tokens[match.token]);
          transpositionMatch.add(new VertexMatch.WithToken(match.vertex, match.vertexRank, tokens[match.token]));
        }
        transposedTokens.add(transpositionMatch);
      }

      merge(graph, Arrays.asList(tokens), matchedTokens);
      mergeTranspositions(graph, transposedTokens);
    }
  }
}
