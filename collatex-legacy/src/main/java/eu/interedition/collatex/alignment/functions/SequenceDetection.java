package eu.interedition.collatex.alignment.functions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.BaseElement;

public class SequenceDetection {

  @SuppressWarnings("boxing")
  public static <T extends BaseElement> List<MatchSequence<T>> calculateMatchSequences(final Set<Match<T>> matches) {
    // sort Matches for the base
    final List<Match<T>> matchesSortedForBase = SequenceDetection.sortMatchesForBase(matches);
    // sort Matches for the witness
    final List<Match<T>> matchesSortedForWitness = SequenceDetection.sortMatchesForWitness(matches);
    // now compare..
    final Map<Match<T>, Match<T>> previousMatchMapBase = SequenceDetection.buildPreviousMatchMap(matchesSortedForBase);
    final Map<Match<T>, Match<T>> previousMatchMapWitness = SequenceDetection.buildPreviousMatchMap(matchesSortedForWitness);
    final List<MatchSequence<T>> sequences = Lists.newArrayList();
    MatchSequence sequence = new MatchSequence(sequences.size());
    for (final Match match : matchesSortedForBase) {
      final Match expected = previousMatchMapBase.get(match);
      final Match actual = previousMatchMapWitness.get(match);
      if (expected != actual || expected != null && !expected.equals(actual)) {
        if (!sequence.isEmpty()) sequences.add(sequence);
        sequence = new MatchSequence(sequences.size());
      }
      sequence.add(match);
    }
    if (!sequence.isEmpty()) sequences.add(sequence);
    return sequences;
  }

  // Note: THE previous METHOD ASSUMED AN ORDER IN THE SET!
  // TODO add test!
  private static <T extends BaseElement> List<Match<T>> sortMatchesForBase(final Set<Match<T>> matches) {
    final Comparator<Match<T>> comparator = new Comparator<Match<T>>() {
      public int compare(final Match<T> o1, final Match<T> o2) {
        return o1.getBaseWord().getBeginPosition() - o2.getBaseWord().getBeginPosition();
      }
    };
    final List<Match<T>> matchesSortedForBase = Lists.newArrayList(matches);
    Collections.sort(matchesSortedForBase, comparator);
    return matchesSortedForBase;
  }

  private static <T extends BaseElement> List<Match<T>> sortMatchesForWitness(final Set<Match<T>> matches) {
    final Comparator<Match> comparator = new Comparator<Match>() {
      public int compare(final Match o1, final Match o2) {
        return o1.getWitnessWord().getBeginPosition() - o2.getWitnessWord().getBeginPosition();
      }
    };
    final List<Match<T>> matchesForWitness = Lists.newArrayList(matches);
    Collections.sort(matchesForWitness, comparator);
    return matchesForWitness;
  }

  private static <T extends BaseElement> Map<Match<T>, Match<T>> buildPreviousMatchMap(final List<Match<T>> matches) {
    final Map<Match<T>, Match<T>> previousMatches = Maps.newHashMap();
    Match previousMatch = null;
    for (final Match match : matches) {
      previousMatches.put(match, previousMatch);
      previousMatch = match;
    }
    return previousMatches;
  }

  public static <T extends BaseElement> List<MatchSequence<T>> sortSequencesForWitness(final List<MatchSequence<T>> matchSequences) {
    final Comparator<MatchSequence<T>> comparator = new Comparator<MatchSequence<T>>() {
      @SuppressWarnings("boxing")
      public int compare(final MatchSequence<T> o1, final MatchSequence<T> o2) {
        return o1.getSegmentPosition() - o2.getSegmentPosition();
      }
    };
    final List<MatchSequence<T>> matchSequencesForWitness = Lists.newArrayList(matchSequences);
    Collections.sort(matchSequencesForWitness, comparator);
    return matchSequencesForWitness;
  }

}
