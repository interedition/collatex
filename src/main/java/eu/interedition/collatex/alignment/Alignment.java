package eu.interedition.collatex.alignment;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.functions.GapDetection;
import eu.interedition.collatex.alignment.functions.SequenceDetection;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class Alignment<T extends BaseElement> {

  private final List<MatchSequence<T>> _sequencesA;
  private final List<MatchSequence<T>> _sequencesB;
  private final Set<Match<T>> _matches;
  private final List<Gap> _gaps;

  // Note: this constructor should take an UnfixedAlignment object as parameter!
  private Alignment(Set<Match<T>> matches, List<Gap> gaps, List<MatchSequence<T>> sequencesA, List<MatchSequence<T>> sequencesB) {
    this._matches = matches;
    this._gaps = gaps;
    this._sequencesA = sequencesA;
    this._sequencesB = sequencesB;
  }

  public static Alignment<Word> create(Set<Match<Word>> matches, Segment a, Segment b) {
    List<MatchSequence<Word>> sequencesA = SequenceDetection.calculateMatchSequences(matches);
    List<MatchSequence<Word>> sequencesB = SequenceDetection.sortSequencesForWitness(sequencesA);
    List<Gap> gaps1 = GapDetection.getVariantsInBetweenMatchSequences(a, b, sequencesA, sequencesB);
    List<Gap> gaps2 = GapDetection.getVariantsInMatchSequences(a, b, sequencesA);
    List<Gap> gaps = Lists.newArrayList();
    gaps.addAll(gaps1);
    gaps.addAll(gaps2);
    return new Alignment<Word>(matches, gaps, sequencesA, sequencesB);
  }

  public Set<Match<T>> getMatches() {
    return _matches;
  }

  public List<MatchSequence<T>> getMatchSequences() {
    return _sequencesA;
  }

  public List<Gap> getGaps() {
    return _gaps;
  }

  public List<MatchSequence<T>> getMatchSequencesOrderedForWitnessA() {
    return getMatchSequences();
  }

  public List<MatchSequence<T>> getMatchSequencesOrderedForWitnessB() {
    return _sequencesB;
  }

  public double getVariationMeasure() {
    return 1000.0 * (_sequencesA.size() - 1) + 10.0 * _gaps.size() + getWordDistanceSum();
  }

  public float getWordDistanceSum() {
    float wordDistanceSum = 0f;
    for (MatchSequence<T> matchSequence : _sequencesA)
      for (Match<T> match : matchSequence.getMatches())
        wordDistanceSum += match.wordDistance;
    return wordDistanceSum;
  }

  public List<Gap> getAdditions() {
    List<Gap> additions = Lists.newArrayList();
    for (Gap gap : _gaps) {
      if (gap.isAddition()) {
        additions.add(gap);
      }
    }
    return additions;
  }

  public List<Gap> getReplacements() {
    List<Gap> replacements = Lists.newArrayList();
    for (Gap gap : _gaps) {
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
