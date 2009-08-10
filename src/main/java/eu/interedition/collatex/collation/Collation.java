package eu.interedition.collatex.collation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.collation.alignment.Match;
import eu.interedition.collatex.collation.gaps.Gap;
import eu.interedition.collatex.collation.gaps.GapDetection;
import eu.interedition.collatex.collation.sequences.MatchSequence;
import eu.interedition.collatex.collation.sequences.SequenceDetection;
import eu.interedition.collatex.input.Witness;

public class Collation {

  private final List<MatchSequence> sequencesA;
  private final List<MatchSequence> sequencesB;
  private final Set<Match> matches;
  private final List<Gap> gaps;

  // Note: this constructor should take only an Alignment object as parameter!
  public Collation(Set<Match> _matches, Witness a, Witness b) {
    this.matches = _matches;
    this.sequencesA = SequenceDetection.calculateMatchSequences(matches);
    this.sequencesB = SequenceDetection.sortSequencesForWitness(sequencesA);
    List<Gap> gaps1 = GapDetection.getVariantsInBetweenMatchSequences(a, b, sequencesA, sequencesB);
    List<Gap> gaps2 = GapDetection.getVariantsInMatchSequences(a, b, sequencesA);
    gaps = Lists.newArrayList();
    gaps.addAll(gaps1);
    gaps.addAll(gaps2);
  }

  public Set<Match> getMatches() {
    return matches;
  }

  public List<MatchSequence> getMatchSequences() {
    return sequencesA;
  }

  public List<Gap> getGaps() {
    return gaps;
  }

  public List<MatchSequence> getMatchSequencesOrderedForWitnessA() {
    return getMatchSequences();
  }

  public List<MatchSequence> getMatchSequencesOrderedForWitnessB() {
    return sequencesB;
  }

  public double getVariationMeasure() {
    return 1000.0 * (sequencesA.size() - 1) + 10.0 * gaps.size() + getWordDistanceSum();
  }

  public float getWordDistanceSum() {
    float wordDistanceSum = 0f;
    for (MatchSequence matchSequence : sequencesA)
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
