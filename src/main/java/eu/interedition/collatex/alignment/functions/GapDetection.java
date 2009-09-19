package eu.interedition.collatex.alignment.functions;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.alignment.Phrase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class GapDetection {
  public static List<Gap> getVariantsInMatchSequences(Witness base, Witness witness, List<MatchSequence> sequences) {
    List<Gap> variants = Lists.newArrayList();
    for (MatchSequence sequence : sequences) {
      List<Match> matches = sequence.getMatches();
      if (matches.size() > 1) {
        Iterator<Match> i = matches.iterator();
        Match previous = i.next();
        while (i.hasNext()) {
          Match next = i.next();
          Word previousWordBase = previous.getBaseWord();
          Word nextWordBase = next.getBaseWord();
          int baseStartPosition = previousWordBase.position;
          int baseEndPosition = nextWordBase.position;
          Word previousWordWitness = previous.getWitnessWord();
          Word nextWordWitness = next.getWitnessWord();
          int witnessStartPosition = previousWordWitness.position;
          int witnessEndPosition = nextWordWitness.position;
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

  public static List<Gap> getVariantsInBetweenMatchSequences(Witness base, Witness witness, List<MatchSequence> sequencesBase, List<MatchSequence> sequencesWitness) {
    List<Phrase> gapsBase = getGapsFromInBetweenMatchSequencesForBase(base, sequencesBase);
    List<Phrase> gapsWitness = getGapsFromInBetweenMatchSequencesForWitness(witness, sequencesWitness);
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

  private static List<Match> getNextMatchesWitness(List<MatchSequence> sequencesWitness) {
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
  private static List<Phrase> getGapsFromInBetweenMatchSequencesForBase(Witness witness, List<MatchSequence> sequences) {
    int currentIndex = 1;
    Word previousWord = null;
    Word nextWord = null;
    List<Phrase> gaps = Lists.newArrayList();
    for (MatchSequence sequence : sequences) {
      int position = sequence.getBasePosition();
      int indexDif = position - currentIndex;
      Match nextMatch = sequence.getFirstMatch();
      nextWord = nextMatch.getBaseWord();
      gaps.add(new Phrase(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getBaseWord();
      currentIndex = 1 + previousWord.position;
    }
    int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new Phrase(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

  // TODO: rename gaps to phrases
  // this method is made for the witness...
  @SuppressWarnings("boxing")
  private static List<Phrase> getGapsFromInBetweenMatchSequencesForWitness(Witness witness, List<MatchSequence> sequences) {
    int currentIndex = 1;
    Word previousWord = null;
    Word nextWord = null;
    List<Phrase> gaps = Lists.newArrayList();
    for (MatchSequence sequence : sequences) {
      int position = sequence.getWitnessPosition();
      int indexDif = position - currentIndex;
      Match nextMatch = sequence.getFirstMatch();
      nextWord = nextMatch.getWitnessWord();
      gaps.add(new Phrase(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getWitnessWord();
      currentIndex = 1 + previousWord.position;
    }
    int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new Phrase(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

}
