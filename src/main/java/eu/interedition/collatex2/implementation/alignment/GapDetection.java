package eu.interedition.collatex2.implementation.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INGram;
import eu.interedition.collatex2.interfaces.IWitness;

public class GapDetection {

  public static List<IGap> detectGap(final List<IMatch> matches, final IWitness witnessA, final IWitness witnessB) {
    final List<INGram> matchingNgramsForA = Lists.newArrayList();
    final List<INGram> matchingNgramsForB = Lists.newArrayList();
    for (final IMatch m : matches) {
      matchingNgramsForA.add(m.getNGramA());
      matchingNgramsForB.add(m.getNGramB());
    }
    final List<INGram> gapNGramsForA = calculateGapNGramsFor(matchingNgramsForA, witnessA);
    final List<INGram> gapNGramsForB = calculateGapNGramsFor(matchingNgramsForB, witnessB);
    final List<IGap> gaps = Lists.newArrayList();
    for (int i = 0; i < gapNGramsForA.size(); i++) {
      final INGram gapA = gapNGramsForA.get(i);
      final INGram gapB = gapNGramsForB.get(i);
      final IGap gap = new Gap(gapA, gapB, null);
      //      final Match<T> nextMatch = nextMatchesWitness.get(i);
      // TODO: move this decision further on the processing chain when sequence detection is added!
      if (!gap.isEmpty()) {
        gaps.add(gap);
      }
    }
    return gaps;
  }

  private static List<INGram> calculateGapNGramsFor(final List<INGram> matchingNgrams, final IWitness witness) {
    int currentIndex = 1;
    INGram previous = null;
    final List<INGram> gaps = Lists.newArrayList();
    for (final INGram current : matchingNgrams) {
      final int position = current.getBeginPosition();
      gaps.add(witness.createNGram(currentIndex, position - 1));
      previous = current;
      currentIndex = 1 + previous.getEndPosition();
    }
    gaps.add(witness.createNGram(currentIndex, witness.size()));
    return gaps;
  }
}
