package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.spike2.collate.Transposition;

public class TranspositionDetection {
  final WitnessIndex witnessIndex;
  final WitnessIndex witnessIndex2;
  private final List<Transposition> transpositions;

  public TranspositionDetection(WitnessIndex _witnessIndex, WitnessIndex _witnessIndex2) {
    this.witnessIndex = _witnessIndex;
    this.witnessIndex2 = _witnessIndex2;
    this.transpositions = detectTranspositions();
  }

  public static List<MatchSequence> calculateMatchSequences(WitnessIndex base, WitnessIndex witness, Set<Match> matches) {
    List<MatchSequence> sequences = Lists.newArrayList();
    MatchSequence sequence = new MatchSequence();
    for (Match match : matches) {
      Integer expected = base.getPreviousWordCode(match.wordCode);
      Integer actual = witness.getPreviousWordCode(match.wordCode);
      if (expected != actual || expected != null && !expected.equals(actual)) {
        if (!sequence.isEmpty()) sequences.add(sequence);
        sequence = new MatchSequence();
      }
      sequence.add(match);
    }
    if (!sequence.isEmpty()) sequences.add(sequence);
    return sequences;
  }

  protected static List<MatchSequence> sortSequencesForWitness(List<MatchSequence> matchSequences) {
    Comparator<MatchSequence> comparator = new Comparator<MatchSequence>() {
      @SuppressWarnings("boxing")
      public int compare(MatchSequence o1, MatchSequence o2) {
        return o1.getWitnessPosition() - o2.getWitnessPosition();
      }
    };
    List<MatchSequence> matchSequencesForWitness = Lists.newArrayList(matchSequences);
    Collections.sort(matchSequencesForWitness, comparator);
    return matchSequencesForWitness;
  }

  protected List<Transposition> detectTranspositions() {
    Matches matches = new Matches(witnessIndex, witnessIndex2);

    List<MatchSequence> matchSequencesForBase = calculateMatchSequences(witnessIndex, witnessIndex2, matches.matches());
    List<MatchSequence> matchSequencesForWitness = sortSequencesForWitness(matchSequencesForBase);

    Trans trans = new Trans(matchSequencesForBase);
    //    System.out.println(trans.getTuples());
    Set<TranspositionTuple> transpositionTuples = trans.getTranspositions();
    List<Transposition> modifications = Lists.newArrayList();
    for (TranspositionTuple transposition : transpositionTuples) {
      int leftPosition = witnessIndex.getPosition(transposition.getLeft());
      int rightPosition = witnessIndex.getPosition(transposition.getRight());
      if (leftPosition > rightPosition) {
        leftPosition = witnessIndex.getPosition(transposition.getRight());
        rightPosition = witnessIndex.getPosition(transposition.getLeft());
      }
      Phrase base = witnessIndex.createPhrase(leftPosition, leftPosition);
      Phrase witness = witnessIndex.createPhrase(rightPosition, rightPosition);
      modifications.add(new Transposition(base, witness));
    }
    return modifications;
  }

  public List<Transposition> getTranspositions() {
    return transpositions;
  }

}
