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

public class SequenceDetection {

  @SuppressWarnings("boxing")
  public static List<MatchSequence> calculateMatchSequences(Set<Match> matches) {
    // sort Matches for the base
    List<Match> matchesSortedForBase = SequenceDetection.sortMatchesForBase(matches);
    // sort Matches for the witness
    List<Match> matchesSortedForWitness = SequenceDetection.sortMatchesForWitness(matches);
    // now compare..
    Map<Match, Match> previousMatchMapBase = SequenceDetection.buildPreviousMatchMap(matchesSortedForBase);
    Map<Match, Match> previousMatchMapWitness = SequenceDetection.buildPreviousMatchMap(matchesSortedForWitness);
    List<MatchSequence> sequences = Lists.newArrayList();
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
  private static List<Match> sortMatchesForBase(Set<Match> matches) {
    Comparator<Match> comparator = new Comparator<Match>() {
      public int compare(Match o1, Match o2) {
        return o1.getBaseWord().position - o2.getBaseWord().position;
      }
    };
    List<Match> matchesSortedForBase = Lists.newArrayList(matches);
    Collections.sort(matchesSortedForBase, comparator);
    return matchesSortedForBase;
  }

  private static List<Match> sortMatchesForWitness(Set<Match> matches) {
    Comparator<Match> comparator = new Comparator<Match>() {
      public int compare(Match o1, Match o2) {
        return o1.getWitnessWord().position - o2.getWitnessWord().position;
      }
    };
    List<Match> matchesForWitness = Lists.newArrayList(matches);
    Collections.sort(matchesForWitness, comparator);
    return matchesForWitness;
  }

  private static Map<Match, Match> buildPreviousMatchMap(List<Match> matches) {
    Map<Match, Match> previousMatches = Maps.newHashMap();
    Match previousMatch = null;
    for (Match match : matches) {
      previousMatches.put(match, previousMatch);
      previousMatch = match;
    }
    return previousMatches;
  }

  public static List<MatchSequence> sortSequencesForWitness(List<MatchSequence> matchSequences) {
    Comparator<MatchSequence> comparator = new Comparator<MatchSequence>() {
      @SuppressWarnings("boxing")
      public int compare(MatchSequence o1, MatchSequence o2) {
        return o1.getSegmentPosition() - o2.getSegmentPosition();
      }
    };
    List<MatchSequence> matchSequencesForWitness = Lists.newArrayList(matchSequences);
    Collections.sort(matchSequencesForWitness, comparator);
    return matchSequencesForWitness;
  }

}
