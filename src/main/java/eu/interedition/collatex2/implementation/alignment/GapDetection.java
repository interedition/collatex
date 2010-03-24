package eu.interedition.collatex2.implementation.alignment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

public class GapDetection {

  static Log LOG = LogFactory.getLog(GapDetection.class);

  public static List<IGap> detectGap(final List<IMatch> matches, final IWitness witnessA, final IWitness witnessB) {
    final List<IPhrase> matchingPhrasesForA = calculateMatchingPhrasesForA(matches);
    final List<IPhrase> matchingPhrasesForB = calculateMatchingPhrasesForB(matches);
    final List<IPhrase> gapPhrasesForA = calculateGapPhrasesFor(matchingPhrasesForA, witnessA);
    final List<IPhrase> gapPhrasesForB = calculateGapPhrasesFor(matchingPhrasesForB, witnessB);
    final List<IGap> gaps = Lists.newArrayList();
    for (int i = 0; i < gapPhrasesForA.size(); i++) {
      final IPhrase gapA = gapPhrasesForA.get(i);
      final IPhrase gapB = gapPhrasesForB.get(i);
      final IGap gap = new Gap(gapA, gapB, null);
      //      final Match<T> nextMatch = nextMatchesWitness.get(i);
      gaps.add(gap);
    }
    return gaps;
  }

  private static List<IPhrase> calculateMatchingPhrasesForA(final List<IMatch> matches) {
    final List<IPhrase> matchingNgramsForA = Lists.newArrayList();
    for (final IMatch m : matches) {
      matchingNgramsForA.add(m.getPhraseA());
    }
    return matchingNgramsForA;
  }

  private static List<IPhrase> calculateMatchingPhrasesForB(final List<IMatch> matches) {
    final List<IPhrase> matchingNgramsForB = Lists.newArrayList();
    for (final IMatch m : matches) {
      matchingNgramsForB.add(m.getPhraseB());
    }
    final Comparator<IPhrase> comparator = new Comparator<IPhrase>() {
      public int compare(final IPhrase o1, final IPhrase o2) {
        return o1.getBeginPosition() - o2.getBeginPosition();
      }
    };
    Collections.sort(matchingNgramsForB, comparator);
    return matchingNgramsForB;
  }

  private static List<IPhrase> calculateGapPhrasesFor(final List<IPhrase> matchingNgrams, final IWitness witness) {
    LOG.info(witness);
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
}
