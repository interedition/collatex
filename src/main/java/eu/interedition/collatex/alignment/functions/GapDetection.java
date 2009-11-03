package eu.interedition.collatex.alignment.functions;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.alignment.Phrase;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Segment;

public class GapDetection {
  public static <T extends BaseElement> List<Gap> getVariantsInMatchSequences(Segment base, Segment witness, List<MatchSequence<T>> sequences) {
    List<Gap> variants = Lists.newArrayList();
    for (MatchSequence<T> sequence : sequences) {
      List<Match<T>> matches = sequence.getMatches();
      if (matches.size() > 1) {
        Iterator<Match<T>> i = matches.iterator();
        Match<T> previous = i.next();
        while (i.hasNext()) {
          Match<T> next = i.next();
          T previousWordBase = previous.getBaseWord();
          T nextWordBase = next.getBaseWord();
          int baseStartPosition = previousWordBase.getPosition();
          int baseEndPosition = nextWordBase.getPosition();
          T previousWordWitness = previous.getWitnessWord();
          T nextWordWitness = next.getWitnessWord();
          int witnessStartPosition = previousWordWitness.getPosition();
          int witnessEndPosition = nextWordWitness.getPosition();
          int gapSizeBase = baseEndPosition - baseStartPosition - 1;
          int gapSizeWitness = witnessEndPosition - witnessStartPosition - 1;
          if (gapSizeBase != 0 || gapSizeWitness != 0) {
            //            System.out.println(gapSizeBase + ":" + gapSizeWitness);
            Phrase gapBase = new Phrase(base, gapSizeBase, baseStartPosition + 1, baseEndPosition - 1, previousWordBase, nextWordBase);
            Phrase gapWitness = new Phrase(witness, gapSizeWitness, witnessStartPosition + 1, witnessEndPosition - 1, previousWordWitness, nextWordWitness);
            Gap nonMatch = new Gap(gapBase, gapWitness, next);
            variants.add(nonMatch);
          }
          previous = next;
        }
      }
    }
    return variants;
  }

  public static <T extends BaseElement> List<Gap> getVariantsInBetweenMatchSequences(Segment base, Segment witness, List<MatchSequence<T>> sequencesBase, List<MatchSequence<T>> sequencesWitness) {
    List<Phrase> gapsBase = getGapsFromInBetweenMatchSequencesForBase(base, sequencesBase);
    List<Phrase<T>> gapsWitness = getGapsFromInBetweenMatchSequencesForWitness(witness, sequencesWitness);
    List<Match> nextMatchesWitness = getNextMatchesWitness(sequencesWitness);
    List<Gap> variants = Lists.newArrayList();
    for (int i = 0; i < gapsBase.size(); i++) {
      Phrase gapBase = gapsBase.get(i);
      Phrase gapWitness = gapsWitness.get(i);
      Match nextMatch = nextMatchesWitness.get(i);
      if (gapBase.hasGap() || gapWitness.hasGap()) {
        Gap nonMatch = new Gap(gapBase, gapWitness, nextMatch);
        variants.add(nonMatch);
      }
    }
    return variants;
  }

  private static <T extends BaseElement> List<Match> getNextMatchesWitness(List<MatchSequence<T>> sequencesWitness) {
    List<Match> nextMatches = Lists.newArrayList();
    for (MatchSequence sequence : sequencesWitness) {
      Match nextMatch = sequence.getFirstMatch();
      nextMatches.add(nextMatch);
    }
    // Note: the last gap does not have a next match!
    nextMatches.add(null);
    return nextMatches;
  }

  // TODO: rename gaps to phrases
  // this method is made for the base... 
  @SuppressWarnings("boxing")
  private static <T extends BaseElement> List<Phrase> getGapsFromInBetweenMatchSequencesForBase(Segment witness, List<MatchSequence<T>> sequences) {
    int currentIndex = 1;
    T previousWord = null;
    T nextWord = null;
    List<Phrase> gaps = Lists.newArrayList();
    for (MatchSequence<T> sequence : sequences) {
      int position = sequence.getBasePosition();
      int indexDif = position - currentIndex;
      Match<T> nextMatch = sequence.getFirstMatch();
      nextWord = nextMatch.getBaseWord();
      gaps.add(new Phrase(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getBaseWord();
      currentIndex = 1 + previousWord.getPosition();
    }
    int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new Phrase(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

  // TODO: rename gaps to phrases
  // this method is made for the witness...
  @SuppressWarnings("boxing")
  private static <T extends BaseElement> List<Phrase<T>> getGapsFromInBetweenMatchSequencesForWitness(Segment witness, List<MatchSequence<T>> sequences) {
    int currentIndex = 1;
    T previousWord = null;
    T nextWord = null;
    List<Phrase<T>> gaps = Lists.newArrayList();
    for (MatchSequence<T> sequence : sequences) {
      int position = sequence.getSegmentPosition();
      int indexDif = position - currentIndex;
      Match<T> nextMatch = sequence.getFirstMatch();
      nextWord = nextMatch.getWitnessWord();
      gaps.add(new Phrase(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getWitnessWord();
      currentIndex = 1 + previousWord.getPosition();
    }
    int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new Phrase(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

}
