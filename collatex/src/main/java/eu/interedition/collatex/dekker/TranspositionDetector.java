/**
 * CollateX - a Java library for collating textual sources, for example, to
 * produce an apparatus.
 *
 * Copyright (C) 2010-2012 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.interedition.collatex.dekker;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.interedition.collatex.graph.VariantGraph;

public class TranspositionDetector {

  private static final Logger LOG = LoggerFactory.getLogger(TranspositionDetector.class);
  private static final int MAX_RELATIVE_RANKDIFF = 2;

  public Tuple<List<List<Match>>> detect(List<List<Match>> phraseMatches, VariantGraph base) {
    //rank the variant graph
    base.rank();

    // gather matched ranks into a list ordered by their natural order
    final List<Integer> matchingBaseRanks = Lists.newArrayList();
    final List<Integer> expandedWitnessMatchRanks = Lists.newArrayList();
    for (List<Match> phraseMatch : phraseMatches) {
      matchingBaseRanks.add(phraseMatchRank(phraseMatch));
      for (Match match : phraseMatch) {
        expandedWitnessMatchRanks.add(match.vertex.getRank());
      }
    }
    LOG.info("witnessRanks={}", matchingBaseRanks);
    Collections.sort(matchingBaseRanks);
    List<Integer> expandedMatchingBaseRanks = Lists.newArrayList(expandedWitnessMatchRanks);
    Collections.sort(expandedMatchingBaseRanks);
    LOG.info("matching base ranks={}", matchingBaseRanks);
    LOG.info("expanded baseRanks={}", expandedMatchingBaseRanks);
    LOG.info("expanded witnessRanks={}", expandedWitnessMatchRanks);

    // detect transpositions
    final List<List<Match>> transpositions = Lists.newArrayList();
    final List<List<Match>> rejectedTranspositions = Lists.newArrayList();
    int baseRankIndex = 0;
    Tuple<Integer> previous = new Tuple<Integer>(0, 0);

    for (List<Match> phraseMatch : phraseMatches) {
      int rank = phraseMatchRank(phraseMatch);
      int expectedRank = matchingBaseRanks.get(baseRankIndex);
      Tuple<Integer> current = new Tuple<Integer>(expectedRank, rank);
      LOG.info("expectedRank={}, rank={}", expectedRank, rank);
      if (expectedRank != rank && !isMirrored(previous, current)) {
        int diff = Math.abs(expandedMatchingBaseRanks.indexOf(rank) - expandedWitnessMatchRanks.indexOf(rank));
        int relDiff = diff / phraseMatch.size();
        LOG.info("base rank: {}, witness rank: {}, rank diff: {} (relativeDiff={}) for {} (size {})", new Object[] { expectedRank, rank, diff, relDiff, phraseMatch, phraseMatch.size() });
        if (relDiff < MAX_RELATIVE_RANKDIFF) {
          LOG.info("!! accepted transposition !!");
          transpositions.add(phraseMatch);
        } else {
          LOG.info("!! rejected transposition !!");
          rejectedTranspositions.add(phraseMatch);
          matchingBaseRanks.remove(Integer.valueOf(rank));
          for (int i = rank; i < rank + phraseMatch.size(); i++) {
            expandedMatchingBaseRanks.remove(Integer.valueOf(i));
            expandedWitnessMatchRanks.remove(Integer.valueOf(i));
          }
          LOG.info("ranks={}", matchingBaseRanks);
          LOG.info("expanded baseRanks={}", expandedMatchingBaseRanks);
          LOG.info("expanded witnessRanks={}", expandedWitnessMatchRanks);
        }
      }
      baseRankIndex++;
      previous = current;
    }
    if (LOG.isTraceEnabled()) {
      for (List<Match> transposition : transpositions) {
        LOG.trace("Detected transposition: {}", Iterables.toString(transposition));
      }
      for (List<Match> transposition : rejectedTranspositions) {
        LOG.trace("Rejected transposition: {}", Iterables.toString(transposition));
      }
    }
    return new Tuple<List<List<Match>>>(transpositions, rejectedTranspositions);
  }

  private int phraseMatchRank(List<Match> phraseMatch) {
    return phraseMatch.get(0).vertex.getRank();
  }

  private boolean isMirrored(Tuple previousTuple, Tuple tuple) {
    return previousTuple.left.equals(tuple.right) && previousTuple.right.equals(tuple.left);
  }
}
