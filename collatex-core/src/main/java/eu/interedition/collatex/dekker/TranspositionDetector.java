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
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.util.VariantGraphRanking;

/**
 * 
 * @author Ronald Haentjens Dekker
 */
public class TranspositionDetector {

  private static final Logger LOG = Logger.getLogger(TranspositionDetector.class.getName());

  public List<List<Match>> detect(List<List<Match>> phraseMatches, VariantGraph base) {
    // rank the variant graph
    Set<VariantGraph.Vertex> matchedVertices = Sets.newHashSet();
    for (List<Match> phraseMatch : phraseMatches) {
      matchedVertices.add(phraseMatch.get(0).vertex);
    }
    final VariantGraphRanking ranking = VariantGraphRanking.ofOnlyCertainVertices(base, null, matchedVertices);

    boolean falseTranspositions;
    final Stack<List<Match>> transpositions = new Stack<List<Match>>();

    do {
      falseTranspositions = false;
      transpositions.clear();

      // gather matched ranks into a list ordered by their natural order
      final List<Integer> phraseWitnessRanks = Lists.newArrayList();
      for (List<Match> phraseMatch : phraseMatches) {
        phraseWitnessRanks.add(Preconditions.checkNotNull(ranking.apply(phraseMatch.get(0).vertex)));
      }
      List<Integer> phraseGraphRanks = Lists.newArrayList(phraseWitnessRanks);
      Collections.sort(phraseGraphRanks);
      
      // detect transpositions
      int previousRank = 0;
      Tuple<Integer> previous = new Tuple<Integer>(0, 0);
      for (List<Match> phraseMatch : phraseMatches) {
        int rank = ranking.apply(phraseMatch.get(0).vertex);
        int expectedRank = phraseGraphRanks.get(previousRank);
        Tuple<Integer> current = new Tuple<Integer>(expectedRank, rank);
        if (expectedRank != rank) {
          addNewTransposition(transpositions, phraseMatch, isMirrored(previous, current));
        }
        previousRank++;
        previous = current;
      }

      // filter away small transposed phrase over long distances
      for (List<Match> transposition : transpositions) {
        int rank = ranking.apply(transposition.get(0).vertex);
        // calculate the distance between the transposed phrases
        int indexInGraphRanks = phraseGraphRanks.indexOf(rank);
        int indexInWitnessRanks = phraseWitnessRanks.indexOf(rank);
        int distanceInTokens = 0;
        if (indexInGraphRanks > indexInWitnessRanks) {
          for (int i = indexInGraphRanks; i > indexInWitnessRanks; i--) {
            distanceInTokens += phraseMatches.get(i).size();
          }
        } else {
          for (int i = indexInGraphRanks; i < indexInWitnessRanks; i++) {
            List<Match> phraseMatchInBetween = phraseMatches.get(i);
            distanceInTokens += phraseMatchInBetween.size();
          }
        }
        // check transposed phrase / transposed distance ratio
        if (distanceInTokens > transposition.size() * 3) {
          phraseMatches.remove(transposition);
          falseTranspositions = true;
          break;
        }
      }
    } while (falseTranspositions);
    if (LOG.isLoggable(Level.FINER)) {
      for (List<Match> transposition : transpositions) {
        LOG.log(Level.FINER, "Detected transposition: {0}", Iterables.toString(transposition));
      }
    }
    return transpositions;
  }

  private void addNewTransposition(final Stack<List<Match>> transpositions, List<Match> transposition, boolean isMirrored) {
    if (!isMirrored) {
      transpositions.add(transposition);
    } else {
      /*
       * A mirrored transposition is detected. We have to check size: If
       * previous > current -> remove previous, add current. Otherwise, do
       * nothing.
       */
      List<Match> lastTransposition = transpositions.peek();
      int lastTranspositionSize = determineSize(lastTransposition);
      int transpositionSize = determineSize(transposition);
      if (lastTranspositionSize > transpositionSize) {
        transpositions.pop();
        transpositions.add(transposition);
      }
    }
  }

  /*
   * in case of an a, b / b, a transposition we have to determine whether a or b
   * stays put. the phrase with the most character stays still if the tokens are
   * not simple tokens the phrase with the most tokens stays put
   */
  private int determineSize(List<Match> t) {
    Match firstMatch = t.get(0);
    if (!(firstMatch.token instanceof SimpleToken)) {
      return t.size();
    }
    int charLength = 0;
    for (Match m : t) {
      SimpleToken token = (SimpleToken) m.token;
      charLength += token.getNormalized().length();
    }
    return charLength;
  }

  private boolean isMirrored(Tuple<Integer> previousTuple, Tuple<Integer> tuple) {
    return previousTuple.left.equals(tuple.right) && previousTuple.right.equals(tuple.left);
  }
}
