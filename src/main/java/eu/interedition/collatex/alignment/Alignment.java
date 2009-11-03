package eu.interedition.collatex.alignment;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.functions.GapDetection;
import eu.interedition.collatex.alignment.functions.SequenceDetection;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Segment;

public class Alignment<T extends BaseElement> {

  private final List<MatchSequence<T>> sequencesA;
  private final List<MatchSequence<T>> sequencesB;
  private final Set<Match<T>> matches;
  private final List<Gap> gaps;

  // Note: this constructor should take an UnfixedAlignment object as parameter!
  public Alignment(Set<Match<T>> _matches, Segment a, Segment b) {
    this.matches = _matches;
    this.sequencesA = SequenceDetection.calculateMatchSequences(matches);
    this.sequencesB = SequenceDetection.sortSequencesForWitness(sequencesA);
    List<Gap> gaps1 = GapDetection.getVariantsInBetweenMatchSequences(a, b, sequencesA, sequencesB);
    List<Gap> gaps2 = GapDetection.getVariantsInMatchSequences(a, b, sequencesA);
    gaps = Lists.newArrayList();
    gaps.addAll(gaps1);
    gaps.addAll(gaps2);
  }

  public Set<Match<T>> getMatches() {
    return matches;
  }

  public List<MatchSequence<T>> getMatchSequences() {
    return sequencesA;
  }

  public List<Gap> getGaps() {
    return gaps;
  }

  public List<MatchSequence<T>> getMatchSequencesOrderedForWitnessA() {
    return getMatchSequences();
  }

  public List<MatchSequence<T>> getMatchSequencesOrderedForWitnessB() {
    return sequencesB;
  }

  public double getVariationMeasure() {
    return 1000.0 * (sequencesA.size() - 1) + 10.0 * gaps.size() + getWordDistanceSum();
  }

  public float getWordDistanceSum() {
    float wordDistanceSum = 0f;
    for (MatchSequence<T> matchSequence : sequencesA)
      for (Match match : matchSequence.getMatches())
        wordDistanceSum += match.wordDistance;
    return wordDistanceSum;
  }

  public List<Gap> getAdditions() {
    List<Gap> additions = Lists.newArrayList();
    for (Gap gap : gaps) {
      if (gap.isAddition()) {
        additions.add(gap);
      }
    }
    return additions;
  }

  public List<Gap> getReplacements() {
    List<Gap> replacements = Lists.newArrayList();
    for (Gap gap : gaps) {
      if (gap.isReplacement()) {
        replacements.add(gap);
      }
    }
    return replacements;
  }

  //  // I just need it as a list of matches
  //  List<MatchSequence> matchSequencesForBase = compresult.getMatchSequencesOrderedForWitnessA();
  //  List<MatchSequence> matchSequencesForWitness = compresult.getMatchSequencesOrderedForWitnessB();
  //  List<Match> matchesOrderedForTheWitness = Lists.newArrayList();
  //  for (MatchSequence matchSeq : matchSequencesForWitness) {
  //    for (Match match : matchSeq.getMatches()) {
  //      matchesOrderedForTheWitness.add(match);
  //    }
  //  }
  //  List<Match> matchesOrderedForTheBase = Lists.newArrayList();
  //  for (MatchSequence matchSeq : matchSequencesForBase) {
  //    for (Match match : matchSeq.getMatches()) {
  //      matchesOrderedForTheBase.add(match);
  //    }
  //  }

}
