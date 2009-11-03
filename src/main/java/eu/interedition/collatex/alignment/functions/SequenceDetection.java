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
  public static <T extends BaseElement> List<MatchSequence<T>> calculateMatchSequences(Set<Match<T>> matches) {
    // sort Matches for the base
    List<Match<T>> matchesSortedForBase = SequenceDetection.sortMatchesForBase(matches);
    // sort Matches for the witness
    List<Match<T>> matchesSortedForWitness = SequenceDetection.sortMatchesForWitness(matches);
    // now compare..
    Map<Match<T>, Match<T>> previousMatchMapBase = SequenceDetection.buildPreviousMatchMap(matchesSortedForBase);
    Map<Match<T>, Match<T>> previousMatchMapWitness = SequenceDetection.buildPreviousMatchMap(matchesSortedForWitness);
    List<MatchSequence<T>> sequences = Lists.newArrayList();
    MatchSequence sequence = new MatchSequence(sequences.size());
    for (Match match : matchesSortedForBase) {
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

  // Note: THE previous METHOD ASSUMED AN ORDER IN THE SET!
  // TODO: add test!
  private static <T extends BaseElement> List<Match<T>> sortMatchesForBase(Set<Match<T>> matches) {
    Comparator<Match<T>> comparator = new Comparator<Match<T>>() {
      public int compare(Match<T> o1, Match<T> o2) {
        return o1.getBaseWord().getPosition() - o2.getBaseWord().getPosition();
      }
    };
    List<Match<T>> matchesSortedForBase = Lists.newArrayList(matches);
    Collections.sort(matchesSortedForBase, comparator);
    return matchesSortedForBase;
  }

  private static <T extends BaseElement> List<Match<T>> sortMatchesForWitness(Set<Match<T>> matches) {
    Comparator<Match> comparator = new Comparator<Match>() {
      public int compare(Match o1, Match o2) {
        return o1.getWitnessWord().getPosition() - o2.getWitnessWord().getPosition();
      }
    };
    List<Match<T>> matchesForWitness = Lists.newArrayList(matches);
    Collections.sort(matchesForWitness, comparator);
    return matchesForWitness;
  }

  private static <T extends BaseElement> Map<Match<T>, Match<T>> buildPreviousMatchMap(List<Match<T>> matches) {
    Map<Match<T>, Match<T>> previousMatches = Maps.newHashMap();
    Match previousMatch = null;
    for (Match match : matches) {
      previousMatches.put(match, previousMatch);
      previousMatch = match;
    }
    return previousMatches;
  }

  public static <T extends BaseElement> List<MatchSequence<T>> sortSequencesForWitness(List<MatchSequence<T>> matchSequences) {
    Comparator<MatchSequence<T>> comparator = new Comparator<MatchSequence<T>>() {
      @SuppressWarnings("boxing")
      public int compare(MatchSequence<T> o1, MatchSequence<T> o2) {
        return o1.getSegmentPosition() - o2.getSegmentPosition();
      }
    };
    List<MatchSequence<T>> matchSequencesForWitness = Lists.newArrayList(matchSequences);
    Collections.sort(matchSequencesForWitness, comparator);
    return matchSequencesForWitness;
  }

}
