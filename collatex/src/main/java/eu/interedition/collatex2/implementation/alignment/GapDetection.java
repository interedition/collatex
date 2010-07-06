/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
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

package eu.interedition.collatex2.implementation.alignment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

public class GapDetection {

  static Logger LOG = LoggerFactory.getLogger(GapDetection.class);

  public static List<IGap> detectGap(final List<IMatch> matches, final IAlignmentTable witnessA, final IWitness witnessB) {
    final List<IColumns> matchingColumnsForA = calculateMatchingColumnsForA(matches);
    final List<IPhrase> matchingPhrasesForB = calculateMatchingPhrasesForB(matches);
    final List<IColumns> gapColumnsForA = calculateGapColumnsForA(matchingColumnsForA, witnessA);
    final List<IPhrase> gapPhrasesForB = calculateGapPhrasesFor(matchingPhrasesForB, witnessB);
    final List<IColumn> nextColumns = getNextColumns(matchingColumnsForA);
    final List<IGap> gaps = Lists.newArrayList();
    for (int i = 0; i < gapColumnsForA.size(); i++) {
      final IColumns gapA = gapColumnsForA.get(i);
      final IPhrase gapB = gapPhrasesForB.get(i);
      final IColumn nextMatchToken = nextColumns.get(i);
      final IGap gap = new Gap(gapA, gapB, nextMatchToken);
      gaps.add(gap);
    }
    return gaps;
  }

  private static List<IColumns> calculateMatchingColumnsForA(final List<IMatch> matches) {
    final List<IColumns> matchingNgramsForA = Lists.newArrayList();
    for (final IMatch m : matches) {
      matchingNgramsForA.add(m.getColumns());
    }
    return matchingNgramsForA;
  }

  private static List<IPhrase> calculateMatchingPhrasesForB(final List<IMatch> matches) {
    final List<IPhrase> matchingNgramsForB = Lists.newArrayList();
    for (final IMatch m : matches) {
      matchingNgramsForB.add(m.getPhrase());
    }
    final Comparator<IPhrase> comparator = new Comparator<IPhrase>() {
      public int compare(final IPhrase o1, final IPhrase o2) {
        return o1.getBeginPosition() - o2.getBeginPosition();
      }
    };
    Collections.sort(matchingNgramsForB, comparator);
    return matchingNgramsForB;
  }

  private static List<IColumns> calculateGapColumnsForA(final List<IColumns> matchingNgrams, final IAlignmentTable table) {
    LOG.debug(table.toString());
    int currentIndex = 1;
    IColumns previous = null;
    final List<IColumns> gaps = Lists.newArrayList();
    for (final IColumns current : matchingNgrams) {
      final int position = current.getBeginPosition();
      gaps.add(table.createColumns(currentIndex, position - 1));
      previous = current;
      currentIndex = 1 + previous.getEndPosition();
    }
    gaps.add(table.createColumns(currentIndex, table.size()));
    return gaps;
  }

  private static List<IPhrase> calculateGapPhrasesFor(final List<IPhrase> matchingNgrams, final IWitness witness) {
    LOG.debug(witness.toString());
    int currentIndex = 1;
    IPhrase previous = null;
    final List<IPhrase> gaps = Lists.newArrayList();
    for (final IPhrase current : matchingNgrams) {
      final int position = current.getBeginPosition();
      gaps.add(witness.createPhrase(currentIndex, position - 1));
      previous = current;
      currentIndex = 1 + previous.getEndPosition();
    }
    gaps.add(witness.createPhrase(currentIndex, witness.size()));
    return gaps;
  }

  //TODO add test!
  private static List<IColumn> getNextColumns(final List<IColumns> matchPhrases) {
    final List<IColumn> nextMatches = Lists.newArrayList();
    for (final IColumns phrase : matchPhrases) {
      final IColumn nextMatchToken = phrase.getFirstColumn();
      nextMatches.add(nextMatchToken);
    }
    // Note: the last gap does not have a next match!
    nextMatches.add(null);
    return nextMatches;
  }

}
