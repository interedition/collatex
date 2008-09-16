package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

  public static List<Tuple2<MatchSequence>> calculateSequenceTuples(List<MatchSequence> matchSequencesForBase, List<MatchSequence> matchSequencesForWitness) {
    List<Tuple2<MatchSequence>> tuples = Lists.newArrayList();
    for (int i = 0; i < matchSequencesForBase.size(); i++) {
      Tuple2<MatchSequence> tuple = new Tuple2<MatchSequence>(matchSequencesForBase.get(i), matchSequencesForWitness.get(i));
      tuples.add(tuple);
    }
    return tuples;
  }

  public static List<Tuple2<MatchSequence>> filterAwayRealMatches(List<Tuple2<MatchSequence>> possibleMatches) {
    List<Tuple2<MatchSequence>> filteredMatchSequences = Lists.newArrayList(Iterables.filter(possibleMatches, new Predicate<Tuple2<MatchSequence>>() {
      public boolean apply(Tuple2<MatchSequence> tuple) {
        return tuple.left.getFirstMatch().wordCode != tuple.right.getFirstMatch().wordCode;
      }
    }));
    return filteredMatchSequences;
  }

  protected static Set<TranspositionTuple> calculateTranspositions(final List<Tuple2<MatchSequence>> possibleTranspositionTuples) {
    // here we go and filter.. so that only transpositions are kept..
    // later on we filter away duplicates
    List<Tuple2<MatchSequence>> matchSequencesInTransposition = Lists.newArrayList(Iterables.filter(possibleTranspositionTuples, new Predicate<Tuple2<MatchSequence>>() {
      public boolean apply(Tuple2<MatchSequence> tuple) {
        Tuple2<MatchSequence> mirror = new Tuple2<MatchSequence>(tuple.right, tuple.left);
        return possibleTranspositionTuples.contains(mirror);
      }
    }));
    // this is to filter away duplicates...
    List<TranspositionTuple> asTranspositionTuples = Lists.newArrayList();
    for (Tuple2<MatchSequence> sequence : matchSequencesInTransposition) {
      asTranspositionTuples.add(new TranspositionTuple(sequence));
    }
    return Sets.newHashSet(asTranspositionTuples);
  }

  protected List<Transposition> detectTranspositions() {
    Matches matches = new Matches(witnessIndex, witnessIndex2);

    List<MatchSequence> matchSequencesForBase = calculateMatchSequences(witnessIndex, witnessIndex2, matches.matches());
    List<MatchSequence> matchSequencesForWitness = sortSequencesForWitness(matchSequencesForBase);
    List<Tuple2<MatchSequence>> matchSequenceTuples = calculateSequenceTuples(matchSequencesForBase, matchSequencesForWitness);
    List<Tuple2<MatchSequence>> possibleTranspositionTuples = filterAwayRealMatches(matchSequenceTuples);
    Set<TranspositionTuple> transpositionTuples = calculateTranspositions(possibleTranspositionTuples);

    List<Transposition> modifications = Lists.newArrayList();
    for (TranspositionTuple transposition : transpositionTuples) {
      MatchSequence base = transposition.getLeftSequence();
      MatchSequence witness = transposition.getRightSequence();
      modifications.add(new Transposition(base, witness));
    }
    return modifications;
  }

  public List<Transposition> getTranspositions() {
    return transpositions;
  }

}
