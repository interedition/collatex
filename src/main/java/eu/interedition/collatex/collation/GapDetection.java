package eu.interedition.collatex.collation;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.Modification;

import eu.interedition.collatex.collation.sequences.MatchSequence;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class GapDetection {
  public static List<NonMatch> getVariantsInMatchSequences(Witness base, Witness witness, List<MatchSequence> sequences) {
    List<NonMatch> variants = Lists.newArrayList();
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
            Gap gapBase = new Gap(base, gapSizeBase, baseStartPosition + 1, baseEndPosition - 1, previousWordBase, nextWordBase);
            Gap gapWitness = new Gap(witness, gapSizeWitness, witnessStartPosition + 1, witnessEndPosition - 1, previousWordWitness, nextWordWitness);
            NonMatch nonMatch = new NonMatch(gapBase, gapWitness);
            variants.add(nonMatch);
          }
          previous = next;
        }
      }
    }
    return variants;
  }

  @Deprecated
  public static List<Modification> analyseVariants(List<NonMatch> variants) {
    List<Modification> results = Lists.newArrayList();
    for (NonMatch nonMatch : variants) {
      Modification modification = nonMatch.analyse();
      results.add(modification);
    }
    return results;
  }

  public static List<NonMatch> getVariantsInBetweenMatchSequences(Witness base, Witness witness, List<MatchSequence> sequencesBase, List<MatchSequence> sequencesWitness) {
    List<Gap> gapsBase = getGapsFromInBetweenMatchSequencesForBase(base, sequencesBase);
    List<Gap> gapsWitness = getGapsFromInBetweenMatchSequencesForWitness(witness, sequencesWitness);
    List<NonMatch> variants = Lists.newArrayList();
    for (int i = 0; i < gapsBase.size(); i++) {
      Gap gapBase = gapsBase.get(i);
      Gap gapWitness = gapsWitness.get(i);
      if (gapBase.hasGap() || gapWitness.hasGap()) {
        NonMatch nonMatch = new NonMatch(gapBase, gapWitness);
        variants.add(nonMatch);
      }
    }
    return variants;
  }

  // this method is made for the base... 
  @SuppressWarnings("boxing")
  private static List<Gap> getGapsFromInBetweenMatchSequencesForBase(Witness witness, List<MatchSequence> sequences) {
    int currentIndex = 1;
    Word previousWord = null;
    Word nextWord = null;
    List<Gap> gaps = Lists.newArrayList();
    for (MatchSequence sequence : sequences) {
      int position = sequence.getBasePosition();
      int indexDif = position - currentIndex;
      nextWord = sequence.getFirstMatch().getBaseWord();
      gaps.add(new Gap(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getBaseWord();
      currentIndex = 1 + previousWord.position;
    }
    int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new Gap(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

  // this method is made for the witness...
  @SuppressWarnings("boxing")
  private static List<Gap> getGapsFromInBetweenMatchSequencesForWitness(Witness witness, List<MatchSequence> sequences) {
    int currentIndex = 1;
    Word previousWord = null;
    Word nextWord = null;
    List<Gap> gaps = Lists.newArrayList();
    for (MatchSequence sequence : sequences) {
      int position = sequence.getWitnessPosition();
      int indexDif = position - currentIndex;
      nextWord = sequence.getFirstMatch().getWitnessWord();
      gaps.add(new Gap(witness, indexDif, currentIndex, position - 1, previousWord, nextWord));
      previousWord = sequence.getLastMatch().getWitnessWord();
      currentIndex = 1 + previousWord.position;
    }
    int IndexDif = witness.size() - currentIndex + 1;
    nextWord = null;
    gaps.add(new Gap(witness, IndexDif, currentIndex, witness.size(), previousWord, nextWord));
    return gaps;
  }

}
