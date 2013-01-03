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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
*
* @author Ronald Haentjens Dekker
*/
public class TranspositionDetector {

  private static final Logger LOG = Logger.getLogger(TranspositionDetector.class.getName());

  public List<List<Match>> detect(List<List<Match>> phraseMatches, VariantGraph base) {
    //rank the variant graph
    final VariantGraphRanking ranking = VariantGraphRanking.of(base);

    // gather matched ranks into a list ordered by their natural order
    final List<Integer> phraseRanks = Lists.newArrayList();
    for (List<Match> phraseMatch : phraseMatches) {
      phraseRanks.add(Preconditions.checkNotNull(ranking.apply(phraseMatch.get(0).vertex)));
    }
    Collections.sort(phraseRanks);

    // detect transpositions
    final List<List<Match>> transpositions = Lists.newArrayList();
    int previousRank = 0;
    Tuple<Integer> previous = new Tuple<Integer>(0, 0);

    for (List<Match> phraseMatch : phraseMatches) {
      int rank = ranking.apply(phraseMatch.get(0).vertex);
      int expectedRank = phraseRanks.get(previousRank);
      Tuple<Integer> current = new Tuple<Integer>(expectedRank, rank);
      if (expectedRank != rank && !isMirrored(previous, current)) {
        transpositions.add(phraseMatch);
      }
      previousRank++;
      previous = current;
    }
    if (LOG.isLoggable(Level.FINER)) {
      for (List<Match> transposition : transpositions) {
        LOG.log(Level.FINER, "Detected transposition: {0}", Iterables.toString(transposition));
      }
    }
    return transpositions;
  }

  private boolean isMirrored(Tuple<Integer> previousTuple, Tuple<Integer> tuple) {
    return previousTuple.left.equals(tuple.right) && previousTuple.right.equals(tuple.left);
  }
}
