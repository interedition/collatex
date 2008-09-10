package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.spike2.collate.Transposition;

public class TranspositionDetection {
  //  private final List<Phrase> phrases;
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

    List<Integer> sequenceOfMatchesInBase = matches.getSequenceOfMatchesInBase();
    //    List<Integer> sequenceOfMatchesInWitness = matches.getSequenceOfMatchesInWitness();
    //System.out.println(sequenceOfMatchesInBase);
    //System.out.println(sequenceOfMatchesInWitness);

    Set<Integer> transposedMatches = calculateTransposedMatches(sequenceOfMatchesInBase);
    List<Integer> sequenceOfTransposedMatchesInBase = Lists.newArrayList(transposedMatches);
    List<Integer> sequenceOfTransposedMatchesInWitness = sortMatchesByPosition(transposedMatches, witnessIndex2);
    //System.out.println(sequenceOfTransposedMatchesInBase);
    //System.out.println(sequenceOfTransposedMatchesInWitness);

    Trans trans = new Trans(sequenceOfTransposedMatchesInBase, sequenceOfTransposedMatchesInWitness);
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

  static class Sequence {
    @SuppressWarnings("boxing")
    public Sequence(int _wordCode, int begin, int end) {
      this.wordCode = _wordCode;
      this.startPosition = begin;
      this.endPosition = end;
    }

    Integer wordCode;
    Integer startPosition;
    Integer endPosition;

    @Override
    public String toString() {
      return wordCode + " -> (" + startPosition + " - " + endPosition + ")";
    }
  }

  // step 1 take the matches
  // step 2 walk over the witness index and filter away everything that is not a match

  protected static List<Integer> sortMatchesByPosition(final Set<Integer> matches, WitnessIndex witness) {
    List<Integer> wordCodesList = witness.getWordCodesList();
    List<Integer> onlyMatches = Lists.newArrayList(Iterables.filter(wordCodesList, new Predicate<Integer>() {
      public boolean apply(Integer wordCode) {
        return matches.contains(wordCode);
      }
    }));
    return onlyMatches;
  }

  // step 1
  // given a sequence map 
  // and a sequence of matches
  // 

  //  public class MatchInfo {
  //    public MatchInfo(Integer wordCode, boolean inSequence) {
  //      this.wordCode = wordCode;
  //      this.inSequence = inSequence;
  //    }
  //
  //    Integer wordCode;
  //    boolean inSequence;
  //  }

  protected List<Phrase> testInSequence(List<Integer> sequenceOfMatchesInBase) {
    final Set<Integer> transposedMatches = calculateTransposedMatches(sequenceOfMatchesInBase);
    List<Integer> sequenceOfTransposedMatchesInBase = Lists.newArrayList(Iterables.filter(sequenceOfMatchesInBase, new Predicate<Integer>() {
      public boolean apply(Integer wordCodeMatch) {
        return transposedMatches.contains(wordCodeMatch);
      }
    }));
    System.out.println(sequenceOfTransposedMatchesInBase);
    List<Phrase> makePhrases = makePhrases(sequenceOfTransposedMatchesInBase, witnessIndex2);
    makePhrases(sequenceOfTransposedMatchesInBase, witnessIndex);
    return makePhrases;
  }

  private Set<Integer> calculateTransposedMatches(List<Integer> sequenceOfMatchesInBase) {
    final Set<Integer> transposedMatches = Sets.newLinkedHashSet(Iterables.filter(sequenceOfMatchesInBase, new Predicate<Integer>() {
      public boolean apply(Integer current) {
        Integer expectedNext = witnessIndex.getPreviousWordCode(current);
        Integer actualNext = witnessIndex2.getPreviousWordCode(current);
        return expectedNext != actualNext;
      }
    }));
    return transposedMatches;
  }

  @SuppressWarnings("boxing")
  protected List<Phrase> makePhrases(List<Integer> sequenceOfTransposedMatchesInBase, final WitnessIndex witness) {
    List<Sequence> sequences = createSequenceList(sequenceOfTransposedMatchesInBase, witness);
    List<Phrase> _phrases = Lists.newArrayList();
    for (Sequence sequence : sequences) {
      _phrases.add(witness.createPhrase(sequence.startPosition, sequence.endPosition));
    }
    return _phrases;

  }

  // step 1: zet 1 sequence of transposed matches to positions, sort them
  // step 2: fold ... initial value of folding is a list with one phrase from 1 to size of witness
  // step 3: in fold make new phrase where necessary

  @SuppressWarnings("boxing")
  private List<Sequence> createSequenceList(List<Integer> sequenceOfTransposedMatchesInBase, final WitnessIndex witness) {
    List<Integer> positionsThatStartARange = Lists.newArrayList(Iterables.transform(sequenceOfTransposedMatchesInBase, new Function<Integer, Integer>() {
      public Integer apply(Integer wordCodeThatStartsRange) {
        return witness.getPosition(wordCodeThatStartsRange);
      }
    }));
    Collections.sort(positionsThatStartARange);
    positionsThatStartARange.remove(new Integer(1)); // TODO: not so nice
    List<Sequence> sequences = Lists.newArrayList();
    sequences.add(new Sequence(witness.getWordCodeOnPosition(1), 1, witness.size())); // TODO: not so nice
    for (Integer position : positionsThatStartARange) {
      Sequence last = sequences.get(sequences.size() - 1);
      last.endPosition = position - 1;
      sequences.add(new Sequence(witness.getWordCodeOnPosition(position), position, witness.size()));
    }
    System.out.println(sequences);
    return sequences;
  }

  //  public List<Phrase> getPhrases() {
  //    return phrases;
  //  }
  //
  public List<Transposition> getTranspositions() {
    return transpositions;
  }

}
