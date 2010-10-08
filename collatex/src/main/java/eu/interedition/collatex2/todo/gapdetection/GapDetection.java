package eu.interedition.collatex2.todo.gapdetection;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IInternalColumn;
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
    final List<IInternalColumn> nextColumns = getNextColumns(matchingColumnsForA);
    final List<IGap> gaps = Lists.newArrayList();
    for (int i = 0; i < gapColumnsForA.size(); i++) {
      final IColumns gapA = gapColumnsForA.get(i);
      final IPhrase gapB = gapPhrasesForB.get(i);
      final IInternalColumn nextMatchToken = nextColumns.get(i);
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
      @Override
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
  private static List<IInternalColumn> getNextColumns(final List<IColumns> matchPhrases) {
    final List<IInternalColumn> nextMatches = Lists.newArrayList();
    for (final IColumns phrase : matchPhrases) {
      final IInternalColumn nextMatchToken = phrase.getFirstColumn();
      nextMatches.add(nextMatchToken);
    }
    // Note: the last gap does not have a next match!
    nextMatches.add(null);
    return nextMatches;
  }

}
