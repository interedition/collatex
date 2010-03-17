package eu.interedition.collatex2.implementation.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.modifications.Transposition;
import eu.interedition.collatex2.interfaces.IAlignment;

public class TranspositionDetection {
  private final IAlignment align;

  public TranspositionDetection(final IAlignment align) {
    this.align = align;
  }

  public List<Transposition> getTranspositions() {
    final List<Transposition> transpositions = Lists.newArrayList();
    return transpositions;
  }

  //  public static <T extends BaseElement> List<Tuple2<MatchSequence<T>>> calculateSequenceTuples(final List<MatchSequence<T>> matchSequencesForBase, final List<MatchSequence<T>> matchSequencesForWitness) {
  //    final List<Tuple2<MatchSequence<T>>> tuples = Lists.newArrayList();
  //    for (int i = 0; i < matchSequencesForBase.size(); i++) {
  //      final IMatch<T> next;
  //      if (i < matchSequencesForBase.size() - 1) {
  //        next = matchSequencesForBase.get(i + 1).getFirstMatch();
  //      } else {
  //        next = null;
  //      }
  //
  //      final Tuple2<MatchSequence<T>> tuple = new Tuple2<MatchSequence<T>>(matchSequencesForBase.get(i), matchSequencesForWitness.get(i), next);
  //      tuples.add(tuple);
  //    }
  //    return tuples;
  //  }
  //
  //  public static <T extends BaseElement> List<Tuple2<MatchSequence<T>>> filterAwayRealMatches(final List<Tuple2<MatchSequence<T>>> possibleMatches) {
  //    final List<Tuple2<MatchSequence<T>>> filteredMatchSequences = Lists.newArrayList(Iterables.filter(possibleMatches, new Predicate<Tuple2<MatchSequence<T>>>() {
  //      public boolean apply(final Tuple2<MatchSequence<T>> tuple) {
  //        return !tuple.left.code.equals(tuple.right.code);
  //      }
  //    }));
  //    return filteredMatchSequences;
  //  }
  //
  //  /**
  //   * simpler replacement for {@link #createTranspositions(List)}
  //   */
  //  public static <T extends BaseElement> List<Transposition> createTranspositions(final List<Tuple2<MatchSequence<T>>> possibleTranspositionTuples) {
  //    List<Transposition> transpositions;
  //    transpositions = Lists.newArrayList();
  //    for (final Tuple2<MatchSequence<T>> possibleTranspositionTuple : possibleTranspositionTuples) {
  //      transpositions.add(new Transposition(possibleTranspositionTuple.right, possibleTranspositionTuple.left, possibleTranspositionTuple._nextMatch));
  //    }
  //    return transpositions;
  //  }
  //
  //  public static List<MatchSequence> getMatches(final List<Tuple2<MatchSequence>> possibleMatches) {
  //    final List<Tuple2<MatchSequence>> filteredMatchSequences = Lists.newArrayList(Iterables.filter(possibleMatches, new Predicate<Tuple2<MatchSequence>>() {
  //      public boolean apply(final Tuple2<MatchSequence> tuple) {
  //        return tuple.left.code.equals(tuple.right.code);
  //      }
  //    }));
  //    final List<MatchSequence> realMatches = Lists.newArrayList();
  //    for (final Tuple2<MatchSequence> tuple2 : filteredMatchSequences) {
  //      realMatches.add(tuple2.left);
  //    }
  //    return realMatches;
  //  }
}
