package com.sd_editions.collatex.permutations;

import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.Block.Util;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Segment;

public class TranspositionDetection {
  final Segment witness;
  final Segment witness2;

  public TranspositionDetection(final Segment _witness, final Segment _witness2) {
    this.witness = _witness;
    this.witness2 = _witness2;
  }

  public static <T extends BaseElement> List<Tuple2<MatchSequence<T>>> calculateSequenceTuples(final List<MatchSequence<T>> matchSequencesForBase, final List<MatchSequence<T>> matchSequencesForWitness) {
    final List<Tuple2<MatchSequence<T>>> tuples = Lists.newArrayList();
    for (int i = 0; i < matchSequencesForBase.size(); i++) {
      final Tuple2<MatchSequence<T>> tuple = new Tuple2<MatchSequence<T>>(matchSequencesForBase.get(i), matchSequencesForWitness.get(i));
      tuples.add(tuple);
    }
    return tuples;
  }

  public static <T extends BaseElement> List<Tuple2<MatchSequence<T>>> filterAwayRealMatches(final List<Tuple2<MatchSequence<T>>> possibleMatches) {
    final List<Tuple2<MatchSequence<T>>> filteredMatchSequences = Lists.newArrayList(Iterables.filter(possibleMatches, new Predicate<Tuple2<MatchSequence<T>>>() {
      public boolean apply(final Tuple2<MatchSequence<T>> tuple) {
        return !tuple.left.code.equals(tuple.right.code);
      }
    }));
    return filteredMatchSequences;
  }

  /**
   * simpler replacement for {@link #createTranspositions(List)}
   */
  public static <T extends BaseElement> List<Transposition> createTranspositions(final List<Tuple2<MatchSequence<T>>> possibleTranspositionTuples) {
    List<Transposition> transpositions;
    transpositions = Lists.newArrayList();
    for (final Tuple2<MatchSequence<T>> possibleTranspositionTuple : possibleTranspositionTuples) {
      transpositions.add(new Transposition(possibleTranspositionTuple.right, possibleTranspositionTuple.left));
    }
    return transpositions;
  }

  /**
   * @deprecated use {@link #createTranspositions(List)} instead
   */
  @Deprecated
  protected static List<Transposition> calculateTranspositions(final List<Tuple2<MatchSequence>> possibleTranspositionTuples) {
    // here we go and filter.. so that only transpositions are kept..
    // later on we filter away duplicates
    Util.p(possibleTranspositionTuples);
    final List<Tuple2<MatchSequence>> matchSequencesInTransposition = Lists.newArrayList(Iterables.filter(possibleTranspositionTuples, new Predicate<Tuple2<MatchSequence>>() {
      public boolean apply(final Tuple2<MatchSequence> tuple) {
        final Tuple2<MatchSequence> mirror = new Tuple2<MatchSequence>(tuple.right, tuple.left);
        return possibleTranspositionTuples.contains(mirror);
      }
    }));
    // this is to filter away duplicates... --> wrap them in TranpositionTuples
    final List<TranspositionTuple> asTranspositionTuples = Lists.newArrayList();
    for (final Tuple2<MatchSequence> sequence : matchSequencesInTransposition) {
      asTranspositionTuples.add(new TranspositionTuple(sequence));
    }
    final Set<TranspositionTuple> transpositionTuples = Sets.newHashSet(asTranspositionTuples);
    // Unwrap them and map them to Transpositions..
    final List<Transposition> modifications = Lists.newArrayList();
    for (final TranspositionTuple transposition : transpositionTuples) {
      final MatchSequence base = transposition.getLeftSequence();
      final MatchSequence witness = transposition.getRightSequence();
      modifications.add(new Transposition(base, witness));
    }
    return modifications;
  }

  public static List<MatchSequence> getMatches(final List<Tuple2<MatchSequence>> possibleMatches) {
    final List<Tuple2<MatchSequence>> filteredMatchSequences = Lists.newArrayList(Iterables.filter(possibleMatches, new Predicate<Tuple2<MatchSequence>>() {
      public boolean apply(final Tuple2<MatchSequence> tuple) {
        return tuple.left.code.equals(tuple.right.code);
      }
    }));
    final List<MatchSequence> realMatches = Lists.newArrayList();
    for (final Tuple2<MatchSequence> tuple2 : filteredMatchSequences) {
      realMatches.add(tuple2.left);
    }
    return realMatches;
  }
}
