package com.sd_editions.collatex.spike2;

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

  protected List<Transposition> detectTranspositions() {
    Matches matches = new Matches(witnessIndex, witnessIndex2);

    List<MatchSequence> calculateMatchSequences = calculateMatchSequences(witnessIndex, witnessIndex2, matches.matches());

    Trans trans = new Trans(calculateMatchSequences);
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
