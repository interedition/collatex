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
  public static <T extends BaseElement> List<Gap> getVariantsInMatchSequences(final BaseContainer base, final BaseContainer witness, final List<MatchSequence<T>> sequences) {
    final List<Gap> variants = Lists.newArrayList();
    for (final MatchSequence<T> sequence : sequences) {
      final List<Match<T>> matches = sequence.getMatches();
      if (matches.size() > 1) {
        final Iterator<Match<T>> i = matches.iterator();
        Match<T> previous = i.next();
        while (i.hasNext()) {
          final Match<T> next = i.next();
          final T previousWordBase = previous.getBaseWord();
          final T nextWordBase = next.getBaseWord();
          final int baseStartPosition = previousWordBase.getPosition();
          final int baseEndPosition = nextWordBase.getPosition();
          final T previousWordWitness = previous.getWitnessWord();
          final T nextWordWitness = next.getWitnessWord();
          final int witnessStartPosition = previousWordWitness.getPosition();
          final int witnessEndPosition = nextWordWitness.getPosition();
          final int gapSizeBase = baseEndPosition - baseStartPosition - 1;
          final int gapSizeWitness = witnessEndPosition - witnessStartPosition - 1;
          if (gapSizeBase != 0 || gapSizeWitness != 0) {
            //            System.out.println(gapSizeBase + ":" + gapSizeWitness);
            final BaseContainerPart<T> gapBase = new BaseContainerPart<T>(base, gapSizeBase, baseStartPosition + 1, baseEndPosition - 1, previousWordBase, nextWordBase);
            final BaseContainerPart<T> gapWitness = new BaseContainerPart<T>(witness, gapSizeWitness, witnessStartPosition + 1, witnessEndPosition - 1, previousWordWitness, nextWordWitness);
            final Gap nonMatch = new Gap(gapBase, gapWitness, next);
            variants.add(nonMatch);
          }
          previous = next;
        }
      }
    }
    return variants;
  }

  public static <T extends BaseElement> List<Gap> getVariantsInBetweenMatchSequences(final BaseContainer base, final BaseContainer witness, final List<MatchSequence<T>> sequencesBase,
      final List<MatchSequence<T>> sequencesWitness) {
    final List<BaseContainerPart<T>> gapsBase = getGapsFromInBetweenMatchSequencesForBase(base, sequencesBase);
    final List<BaseContainerPart<T>> gapsWitness = getGapsFromInBetweenMatchSequencesForWitness(witness, sequencesWitness);
    final List<Match<T>> nextMatchesWitness = getNextMatchesWitness(sequencesWitness);
    final List<Gap> variants = Lists.newArrayList();
    for (int i = 0; i < gapsBase.size(); i++) {
      final BaseContainerPart<T> gapBase = gapsBase.get(i);
      final BaseContainerPart<T> gapWitness = gapsWitness.get(i);
      final Match<T> nextMatch = nextMatchesWitness.get(i);
      if (gapBase.hasGap() || gapWitness.hasGap()) {
        final Gap nonMatch = new Gap(gapBase, gapWitness, nextMatch);
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

  // TODO: rename gaps to parts!
  // this method is made for the base... 
  @SuppressWarnings("boxing")
  private static <T extends BaseElement> List<BaseContainerPart<T>> getGapsFromInBetweenMatchSequencesForBase(final BaseContainer witness, final List<MatchSequence<T>> sequences) {
    int currentIndex = 1;
    T previousWord = null;
    T nextWord = null;
    final List<BaseContainerPart<T>> gaps = Lists.newArrayList();
    for (final MatchSequence<T> sequence : sequences) {
      final int position = sequence.getBasePosition();
      final int indexDif = position - currentIndex;
      final Match<T> nextMatch = sequence.getFirstMatch();
      nextWord = nextMatch.getBaseWord();
      gaps.add(new BaseContainerPart<T>(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getBaseWord();
      currentIndex = 1 + previousWord.getPosition();
    }
    // TODO: rename IndexDif to indexDif
    final int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new BaseContainerPart<T>(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

  // TODO: rename gaps to parts
  // this method is made for the witness...
  @SuppressWarnings("boxing")
  private static <T extends BaseElement> List<BaseContainerPart<T>> getGapsFromInBetweenMatchSequencesForWitness(final BaseContainer witness, final List<MatchSequence<T>> sequences) {
    int currentIndex = 1;
    T previousWord = null;
    T nextWord = null;
    final List<BaseContainerPart<T>> gaps = Lists.newArrayList();
    for (final MatchSequence<T> sequence : sequences) {
      final int position = sequence.getSegmentPosition();
      final int indexDif = position - currentIndex;
      final Match<T> nextMatch = sequence.getFirstMatch();
      nextWord = nextMatch.getWitnessWord();
      gaps.add(new BaseContainerPart<T>(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getWitnessWord();
      currentIndex = 1 + previousWord.getPosition();
    }
    final int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new BaseContainerPart<T>(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

}
