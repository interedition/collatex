package com.sd_editions.collatex.permutations;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.Block.Util;
import com.sd_editions.collatex.permutations.collate.Transposition;

public class TranspositionDetection {
  final Witness witness;
  final Witness witness2;

  public TranspositionDetection(Witness _witness, Witness _witness2) {
    this.witness = _witness;
    this.witness2 = _witness2;
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
        return tuple.left.code != tuple.right.code;
      }
    }));
    return filteredMatchSequences;
  }

  protected static List<Transposition> calculateTranspositions(final List<Tuple2<MatchSequence>> possibleTranspositionTuples) {
    // here we go and filter.. so that only transpositions are kept..
    // later on we filter away duplicates
    Util.p(possibleTranspositionTuples);
    List<Tuple2<MatchSequence>> matchSequencesInTransposition = Lists.newArrayList(Iterables.filter(possibleTranspositionTuples, new Predicate<Tuple2<MatchSequence>>() {
      public boolean apply(Tuple2<MatchSequence> tuple) {
        Tuple2<MatchSequence> mirror = new Tuple2<MatchSequence>(tuple.right, tuple.left);
        return possibleTranspositionTuples.contains(mirror);
      }
    }));
    // this is to filter away duplicates... --> wrap them in TranpositionTuples
    List<TranspositionTuple> asTranspositionTuples = Lists.newArrayList();
    for (Tuple2<MatchSequence> sequence : matchSequencesInTransposition) {
      asTranspositionTuples.add(new TranspositionTuple(sequence));
    }
    Set<TranspositionTuple> transpositionTuples = Sets.newHashSet(asTranspositionTuples);
    // Unwrap them and map them to Transpositions..
    List<Transposition> modifications = Lists.newArrayList();
    for (TranspositionTuple transposition : transpositionTuples) {
      MatchSequence base = transposition.getLeftSequence();
      MatchSequence witness = transposition.getRightSequence();
      modifications.add(new Transposition(base, witness));
    }
    return modifications;
  }

  public static List<MatchSequence> getMatches(List<Tuple2<MatchSequence>> possibleMatches) {
    List<Tuple2<MatchSequence>> filteredMatchSequences = Lists.newArrayList(Iterables.filter(possibleMatches, new Predicate<Tuple2<MatchSequence>>() {
      public boolean apply(Tuple2<MatchSequence> tuple) {
        return tuple.left.code == tuple.right.code;
      }
    }));
    List<MatchSequence> realMatches = Lists.newArrayList();
    for (Tuple2<MatchSequence> tuple2 : filteredMatchSequences) {
      realMatches.add(tuple2.left);
    }
    return realMatches;
  }
}
