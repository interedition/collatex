package eu.interedition.collatex.alignment.functions;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.BaseContainer;
import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.input.Word;

//TODO: remove dependency on Word!
//TODO: make class generic!
public class GapDetection {
  public static List<Gap> getVariantsInMatchSequences(final BaseContainer base, final BaseContainer witness, final List<MatchSequence<Word>> sequences) {
    final List<Gap> variants = Lists.newArrayList();
    for (final MatchSequence<Word> sequence : sequences) {
      final List<Match<Word>> matches = sequence.getMatches();
      if (matches.size() > 1) {
        final Iterator<Match<Word>> i = matches.iterator();
        Match<Word> previous = i.next();
        while (i.hasNext()) {
          final Match<Word> next = i.next();
          final Word previousWordBase = previous.getBaseWord();
          final Word nextWordBase = next.getBaseWord();
          final int baseStartPosition = previousWordBase.getPosition();
          final int baseEndPosition = nextWordBase.getPosition();
          final Word previousWordWitness = previous.getWitnessWord();
          final Word nextWordWitness = next.getWitnessWord();
          final int witnessStartPosition = previousWordWitness.getPosition();
          final int witnessEndPosition = nextWordWitness.getPosition();
          final int gapSizeBase = baseEndPosition - baseStartPosition - 1;
          final int gapSizeWitness = witnessEndPosition - witnessStartPosition - 1;
          if (gapSizeBase != 0 || gapSizeWitness != 0) {
            //            System.out.println(gapSizeBase + ":" + gapSizeWitness);
            final BaseContainerPart gapBase = new BaseContainerPart(base, gapSizeBase, baseStartPosition + 1, baseEndPosition - 1, previousWordBase, nextWordBase);
            final BaseContainerPart gapWitness = new BaseContainerPart(witness, gapSizeWitness, witnessStartPosition + 1, witnessEndPosition - 1, previousWordWitness, nextWordWitness);
            final Gap nonMatch = new Gap(gapBase, gapWitness, next);
            variants.add(nonMatch);
          }
          previous = next;
        }
      }
    }
    return variants;
  }

  public static List<Gap> getVariantsInBetweenMatchSequences(final BaseContainer base, final BaseContainer witness, final List<MatchSequence<Word>> sequencesBase,
      final List<MatchSequence<Word>> sequencesWitness) {
    final List<BaseContainerPart> gapsBase = getGapsFromInBetweenMatchSequencesForBase(base, sequencesBase);
    final List<BaseContainerPart> gapsWitness = getGapsFromInBetweenMatchSequencesForWitness(witness, sequencesWitness);
    final List<Match<Word>> nextMatchesWitness = getNextMatchesWitness(sequencesWitness);
    final List<Gap> variants = Lists.newArrayList();
    for (int i = 0; i < gapsBase.size(); i++) {
      final BaseContainerPart gapBase = gapsBase.get(i);
      final BaseContainerPart gapWitness = gapsWitness.get(i);
      final Match<Word> nextMatch = nextMatchesWitness.get(i);
      if (gapBase.hasGap() || gapWitness.hasGap()) {
        final Gap nonMatch = new Gap(gapBase, gapWitness, nextMatch);
        variants.add(nonMatch);
      }
    }
    return variants;
  }

  private static List<Match<Word>> getNextMatchesWitness(final List<MatchSequence<Word>> sequencesWitness) {
    final List<Match<Word>> nextMatches = Lists.newArrayList();
    for (final MatchSequence<Word> sequence : sequencesWitness) {
      final Match<Word> nextMatch = sequence.getFirstMatch();
      nextMatches.add(nextMatch);
    }
    // Note: the last gap does not have a next match!
    nextMatches.add(null);
    return nextMatches;
  }

  // TODO: rename gaps to parts!
  // this method is made for the base... 
  @SuppressWarnings("boxing")
  private static List<BaseContainerPart> getGapsFromInBetweenMatchSequencesForBase(final BaseContainer witness, final List<MatchSequence<Word>> sequences) {
    int currentIndex = 1;
    Word previousWord = null;
    Word nextWord = null;
    final List<BaseContainerPart> gaps = Lists.newArrayList();
    for (final MatchSequence<Word> sequence : sequences) {
      final int position = sequence.getBasePosition();
      final int indexDif = position - currentIndex;
      final Match<Word> nextMatch = sequence.getFirstMatch();
      nextWord = nextMatch.getBaseWord();
      gaps.add(new BaseContainerPart(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getBaseWord();
      currentIndex = 1 + previousWord.getPosition();
    }
    // TODO: rename IndexDif to indexDif
    final int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new BaseContainerPart(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

  // TODO: rename gaps to parts
  // this method is made for the witness...
  @SuppressWarnings("boxing")
  private static List<BaseContainerPart> getGapsFromInBetweenMatchSequencesForWitness(final BaseContainer witness, final List<MatchSequence<Word>> sequences) {
    int currentIndex = 1;
    Word previousWord = null;
    Word nextWord = null;
    final List<BaseContainerPart> gaps = Lists.newArrayList();
    for (final MatchSequence<Word> sequence : sequences) {
      final int position = sequence.getSegmentPosition();
      final int indexDif = position - currentIndex;
      final Match<Word> nextMatch = sequence.getFirstMatch();
      nextWord = nextMatch.getWitnessWord();
      gaps.add(new BaseContainerPart(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getWitnessWord();
      currentIndex = 1 + previousWord.getPosition();
    }
    final int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new BaseContainerPart(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

}
