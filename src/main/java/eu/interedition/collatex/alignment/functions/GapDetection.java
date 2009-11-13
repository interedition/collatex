package eu.interedition.collatex.alignment.functions;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.BaseContainer;
import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.input.BaseElement;

public class GapDetection {
  // TODO: rename Word to Element
  // TODO: rename some gaps to Part
  public static <T extends BaseElement> List<Gap<T>> getVariantsInMatchSequences(final BaseContainer<T> base, final BaseContainer<T> witness, final List<MatchSequence<T>> sequences) {
    final List<Gap<T>> variants = Lists.newArrayList();
    for (final MatchSequence<T> sequence : sequences) {
      final List<Match<T>> matches = sequence.getMatches();
      if (matches.size() > 1) {
        final Iterator<Match<T>> i = matches.iterator();
        Match<T> previous = i.next();
        while (i.hasNext()) {
          final Match<T> next = i.next();
          final T previousWordBase = previous.getBaseWord();
          final T nextWordBase = next.getBaseWord();
          final int baseStartPosition = previousWordBase.getEndPosition();
          final int baseEndPosition = nextWordBase.getBeginPosition();
          final T previousWordWitness = previous.getWitnessWord();
          final T nextWordWitness = next.getWitnessWord();
          final int witnessStartPosition = previousWordWitness.getEndPosition();
          final int witnessEndPosition = nextWordWitness.getBeginPosition();
          final int gapSizeBase = baseEndPosition - baseStartPosition - 1;
          final int gapSizeWitness = witnessEndPosition - witnessStartPosition - 1;
          if (gapSizeBase != 0 || gapSizeWitness != 0) {
            //            System.out.println(gapSizeBase + ":" + gapSizeWitness);
            final BaseContainerPart<T> gapBase = new BaseContainerPart<T>(base, gapSizeBase, baseStartPosition + 1, baseEndPosition - 1);
            final BaseContainerPart<T> gapWitness = new BaseContainerPart<T>(witness, gapSizeWitness, witnessStartPosition + 1, witnessEndPosition - 1);
            final Gap<T> nonMatch = new Gap<T>(gapBase, gapWitness, next);
            variants.add(nonMatch);
          }
          previous = next;
        }
      }
    }
    return variants;
  }

  // TODO: rename some gaps to Part
  // TODO: rename nonmatch to gap
  public static <T extends BaseElement> List<Gap<T>> getVariantsInBetweenMatchSequences(final BaseContainer<T> base, final BaseContainer<T> witness, final List<MatchSequence<T>> sequencesBase,
      final List<MatchSequence<T>> sequencesWitness) {
    final List<BaseContainerPart<T>> gapsBase = getGapsFromInBetweenMatchSequencesForBase(base, sequencesBase);
    final List<BaseContainerPart<T>> gapsWitness = getGapsFromInBetweenMatchSequencesForWitness(witness, sequencesWitness);
    final List<Match<T>> nextMatchesWitness = getNextMatchesWitness(sequencesWitness);
    final List<Gap<T>> variants = Lists.newArrayList();
    for (int i = 0; i < gapsBase.size(); i++) {
      final BaseContainerPart<T> gapBase = gapsBase.get(i);
      final BaseContainerPart<T> gapWitness = gapsWitness.get(i);
      final Match<T> nextMatch = nextMatchesWitness.get(i);
      if (gapBase.hasGap() || gapWitness.hasGap()) {
        final Gap<T> nonMatch = new Gap<T>(gapBase, gapWitness, nextMatch);
        variants.add(nonMatch);
      }
    }
    return variants;
  }

  private static <T extends BaseElement> List<Match<T>> getNextMatchesWitness(final List<MatchSequence<T>> sequencesWitness) {
    final List<Match<T>> nextMatches = Lists.newArrayList();
    for (final MatchSequence<T> sequence : sequencesWitness) {
      final Match<T> nextMatch = sequence.getFirstMatch();
      nextMatches.add(nextMatch);
    }
    // Note: the last gap does not have a next match!
    nextMatches.add(null);
    return nextMatches;
  }

  // TODO: rename Word to Element!
  // TODO: rename gaps to parts!
  // this method is made for the base... 
  @SuppressWarnings("boxing")
  private static <T extends BaseElement> List<BaseContainerPart<T>> getGapsFromInBetweenMatchSequencesForBase(final BaseContainer<T> witness, final List<MatchSequence<T>> sequences) {
    int currentIndex = 1;
    T previousWord = null;
    final List<BaseContainerPart<T>> gaps = Lists.newArrayList();
    for (final MatchSequence<T> sequence : sequences) {
      // TODO: with getBasePosition the begin position is meant!
      final int position = sequence.getBasePosition();
      final int indexDif = position - currentIndex;
      gaps.add(new BaseContainerPart<T>(witness, indexDif, currentIndex, position - 1));
      previousWord = sequence.getLastMatch().getBaseWord();
      currentIndex = 1 + previousWord.getEndPosition();
    }
    // TODO: rename IndexDif to indexDif
    final int IndexDif = witness.wordSize() - currentIndex + 1;
    gaps.add(new BaseContainerPart<T>(witness, IndexDif, currentIndex, witness.wordSize()));
    return gaps;
  }

  // TODO: rename gaps to parts
  // this method is made for the witness...
  @SuppressWarnings("boxing")
  private static <T extends BaseElement> List<BaseContainerPart<T>> getGapsFromInBetweenMatchSequencesForWitness(final BaseContainer<T> witness, final List<MatchSequence<T>> sequences) {
    int currentIndex = 1;
    T previousWord = null;
    final List<BaseContainerPart<T>> gaps = Lists.newArrayList();
    for (final MatchSequence<T> sequence : sequences) {
      // with getSegmentPosition getWitnessStartPosition is meant!
      final int position = sequence.getSegmentPosition();
      final int indexDif = position - currentIndex;
      gaps.add(new BaseContainerPart<T>(witness, indexDif, currentIndex, position - 1));
      previousWord = sequence.getLastMatch().getWitnessWord();
      currentIndex = 1 + previousWord.getEndPosition();
    }
    final int IndexDif = witness.wordSize() - currentIndex + 1;
    gaps.add(new BaseContainerPart<T>(witness, IndexDif, currentIndex, witness.wordSize()));
    return gaps;
  }

}
