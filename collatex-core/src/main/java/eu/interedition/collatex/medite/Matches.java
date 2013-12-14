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

package eu.interedition.collatex.medite;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Matches extends ArrayList<Phrase<Match.WithTokenIndex>> {

  public Matches(int initialCapacity) {
    super(initialCapacity);
  }

  public static Matches between(VariantGraphRanking ranking, SuffixTree<Token> suffixTree, Function<Phrase<Match.WithTokenIndex>, Integer> matchEvaluator) {

    final SortedSetMultimap<Integer,VariantGraph.Vertex> rankMap = ranking.getByRank();
    final Multimap<Integer, MatchThreadElement> matchThreads = HashMultimap.create();
    for (Integer rank : rankMap.keySet()) {
      final SortedSet<VariantGraph.Vertex> vertices = rankMap.get(rank);
      for (VariantGraph.Vertex vertex : vertices) {
        final MatchThreadElement matchThreadElement = new MatchThreadElement(suffixTree).advance(vertex, rank);
        if (matchThreadElement != null) {
          matchThreads.put(rank, matchThreadElement);
        }
      }
      for (MatchThreadElement matchThreadElement : matchThreads.get(rank - 1)) {
        for (VariantGraph.Vertex vertex : vertices) {
          final MatchThreadElement advanced = matchThreadElement.advance(vertex, rank);
          if (advanced != null) {
            matchThreads.put(rank, advanced);
          }
        }
      }
    }

    final Matches matches = new Matches(matchThreads.size());
    for (MatchThreadElement matchThreadElement : matchThreads.values()) {
      final List<Phrase<Match.WithTokenIndex>> threadPhrases = Lists.newArrayList();
      boolean firstElement = true;
      for (MatchThreadElement threadElement : matchThreadElement.thread()) {
        final SuffixTree<Token>.EquivalenceClass equivalenceClass = threadElement.cursor.matchedClass();
        for (int mc = 0; mc < equivalenceClass.length; mc++) {
          final int tokenCandidate = equivalenceClass.members[mc];
          if (firstElement) {
            final Phrase<Match.WithTokenIndex> phrase = new Phrase<Match.WithTokenIndex>();
            phrase.add(new Match.WithTokenIndex(threadElement.vertex, threadElement.vertexRank, tokenCandidate));
            threadPhrases.add(phrase);
          } else {
            for (Phrase<Match.WithTokenIndex> phrase : threadPhrases) {
              if ((phrase.last().token + 1) == tokenCandidate) {
                phrase.add(new Match.WithTokenIndex(threadElement.vertex, threadElement.vertexRank, tokenCandidate));
              }
            }
          }
        }
        firstElement = false;
      }
      matches.addAll(threadPhrases);
    }
    Collections.sort(matches, maximalUniqueMatchOrdering(matchEvaluator));

    return matches;
  }

  private static Comparator<Phrase<Match.WithTokenIndex>> maximalUniqueMatchOrdering(final Function<Phrase<Match.WithTokenIndex>, Integer> matchEvaluator) {
    return new Comparator<Phrase<Match.WithTokenIndex>>() {
      @Override
      public int compare(Phrase<Match.WithTokenIndex> o1, Phrase<Match.WithTokenIndex> o2) {
        // 1. reverse ordering by match value
        int result = matchEvaluator.apply(o2) - matchEvaluator.apply(o1);
        if (result != 0) {
          return result;
        }

        final Match.WithTokenIndex firstMatch1 = o1.first();
        final Match.WithTokenIndex firstMatch2 = o2.first();

        // 2. ordering by match distance
        result = (Math.abs(firstMatch1.token - firstMatch1.vertexRank) - Math.abs(firstMatch2.token - firstMatch2.vertexRank));
        if (result != 0) {
          return result;
        }


        // 3. ordering by first vertex ranking
        result = firstMatch1.vertexRank - firstMatch2.vertexRank;
        if (result != 0) {
          return result;
        }

        // 3. ordering by first token index
        return firstMatch1.token - firstMatch2.token;

      }
    };
  }

  public SortedSet<Phrase<Match.WithTokenIndex>> findMaximalUniqueMatches() {
    final List<Phrase<Match.WithTokenIndex>> allMatches = Lists.newArrayList(this);
    final SortedSet<Phrase<Match.WithTokenIndex>> maximalUniqueMatches = Sets.newTreeSet();
    while (true) {
      Phrase<Match.WithTokenIndex> nextMum = null;
      Phrase<Match.WithTokenIndex> candidate = null;
      for (Phrase<Match.WithTokenIndex> successor : allMatches) {
        if (candidate == null) {
          continue;
        }
        if (candidate.size() > successor.size() || candidate.first().token == successor.first().token) {
          nextMum = candidate;
          break;
        }
        candidate = successor;
      }
      if (nextMum == null) {
        nextMum = Iterables.getFirst(allMatches, null);
      }
      if (nextMum == null) {
        break;
      }
      Preconditions.checkState(maximalUniqueMatches.add(nextMum), "Duplicate MUM");

      Iterables.removeIf(allMatches, Match.filter(
              new IndexRangeSet(Range.closed(nextMum.first().vertexRank, nextMum.last().vertexRank)),
              new IndexRangeSet(Range.closed(nextMum.first().token, nextMum.last().token))
      ));
    }
    return maximalUniqueMatches;
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  static class MatchThreadElement {

    final MatchThreadElement previous;
    final VariantGraph.Vertex vertex;
    final int vertexRank;
    final SuffixTree<Token>.Cursor cursor;

    MatchThreadElement(SuffixTree<Token> suffixTree) {
      this(null, null, -1, suffixTree.cursor());
    }

    MatchThreadElement(MatchThreadElement previous, VariantGraph.Vertex vertex, int vertexRank, SuffixTree<Token>.Cursor cursor) {
      this.previous = previous;
      this.vertex = vertex;
      this.vertexRank = vertexRank;
      this.cursor = cursor;
    }

    MatchThreadElement advance(VariantGraph.Vertex vertex, int vertexRank) {
      final Set<Token> tokens = vertex.tokens();
      if (!tokens.isEmpty()) {
        final SuffixTree<Token>.Cursor next = cursor.move(Iterables.get(tokens, 0));
        if (next != null) {
          return new MatchThreadElement(this, vertex, vertexRank, next);
        }
      }
      return null;
    }

    List<MatchThreadElement> thread() {
      final LinkedList<MatchThreadElement> thread = Lists.newLinkedList();
      MatchThreadElement current = this;
      while (current.vertex != null) {
        thread.addFirst(current);
        current = current.previous;
      }
      return thread;
    }

    @Override
    public String toString() {
      return "[" + Joiner.on(", ").join(vertexRank, vertex, cursor.matchedClass()) + "]";
    }
  }
}
