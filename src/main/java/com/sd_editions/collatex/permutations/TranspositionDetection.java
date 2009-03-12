package com.sd_editions.collatex.permutations;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

  @SuppressWarnings("boxing")
  public static List<MatchSequence> calculateMatchSequencesForgetNonMatches(Set<Match> matches) {
    // sort Matches for the base --> that is already done (it is a linked hash set)
    List<Match> matchesSortedForBase = sortMatchesForBase(matches);
    // sort Matches for the witness
    List<Match> matchesSortedForWitness = sortMatchesForWitness(matches);
    // now compare..
    Map<Match, Match> previousMatchMapBase = buildPreviousMatchMap(matchesSortedForBase);
    Map<Match, Match> previousMatchMapWitness = buildPreviousMatchMap(matchesSortedForWitness);
    List<MatchSequence> sequences = Lists.newArrayList();
    MatchSequence sequence = new MatchSequence(sequences.size());
    for (Match match : matches) {
      Match expected = previousMatchMapBase.get(match);
      Match actual = previousMatchMapWitness.get(match);
      if (expected != actual || expected != null && !expected.equals(actual)) {
        if (!sequence.isEmpty()) sequences.add(sequence);
        sequence = new MatchSequence(sequences.size());
      }
      sequence.add(match);
    }
    if (!sequence.isEmpty()) sequences.add(sequence);
    return sequences;
  }

  private static List<Match> sortMatchesForBase(Set<Match> matches) {
    List<Match> matchesSortedForBase = Lists.newArrayList();
    for (Match match : matches) {
      matchesSortedForBase.add(match);
    }
    return matchesSortedForBase;
  }

  public static Map<Match, Match> buildPreviousMatchMap(List<Match> matches) {
    Map<Match, Match> previousMatches = Maps.newHashMap();
    Match previousMatch = null;
    for (Match match : matches) {
      previousMatches.put(match, previousMatch);
      previousMatch = match;
    }
    return previousMatches;
  }

  protected static List<Match> sortMatchesForWitness(Set<Match> matches) {
    Comparator<Match> comparator = new Comparator<Match>() {
      public int compare(Match o1, Match o2) {
        return o1.getWitnessWord().position - o2.getWitnessWord().position;
      }
    };
    List<Match> matchesForWitness = Lists.newArrayList(matches);
    Collections.sort(matchesForWitness, comparator);
    return matchesForWitness;
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
