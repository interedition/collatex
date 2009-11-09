package eu.interedition.collatex.alignment;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.TranspositionDetection;
import com.sd_editions.collatex.permutations.Tuple2;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.alignment.functions.GapDetection;
import eu.interedition.collatex.alignment.functions.SequenceDetection;
import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
import eu.interedition.collatex.input.Word;

public class Alignment<T extends BaseElement> {

  private final List<MatchSequence<T>> _sequencesA;
  private final List<MatchSequence<T>> _sequencesB;
  private final Set<Match<T>> _matches;
  private final List<Gap> _gaps;

  // Note: this constructor should take an UnfixedAlignment object as parameter!
  private Alignment(final Set<Match<T>> matches, final List<Gap> gaps, final List<MatchSequence<T>> sequencesA, final List<MatchSequence<T>> sequencesB) {
    this._matches = matches;
    this._gaps = gaps;
    this._sequencesA = sequencesA;
    this._sequencesB = sequencesB;
  }

  public static Alignment<Word> create(final Set<Match<Word>> matches, final Segment a, final Segment b) {
    final List<MatchSequence<Word>> sequencesA = SequenceDetection.calculateMatchSequences(matches);
    final List<MatchSequence<Word>> sequencesB = SequenceDetection.sortSequencesForWitness(sequencesA);
    final List<Gap> gaps1 = GapDetection.getVariantsInBetweenMatchSequences(a, b, sequencesA, sequencesB);
    final List<Gap> gaps2 = GapDetection.getVariantsInMatchSequences(a, b, sequencesA);
    final List<Gap> gaps = Lists.newArrayList();
    gaps.addAll(gaps1);
    gaps.addAll(gaps2);
    return new Alignment<Word>(matches, gaps, sequencesA, sequencesB);
  }

  // TODO: gap detection does not work yet!
  public static Alignment<Phrase> createPhraseAlignment(final Set<Match<Phrase>> matches, final WitnessSegmentPhrases a, final WitnessSegmentPhrases b) {
    final List<MatchSequence<Phrase>> sequencesA = SequenceDetection.calculateMatchSequences(matches);
    final List<MatchSequence<Phrase>> sequencesB = SequenceDetection.sortSequencesForWitness(sequencesA);
    final List<Gap> gaps = Lists.newArrayList();
    final Alignment<Phrase> al = new Alignment<Phrase>(matches, gaps, sequencesA, sequencesB);
    return al;
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
    for (final MatchSequence<T> matchSequence : _sequencesA)
      for (final Match<T> match : matchSequence.getMatches())
        wordDistanceSum += match.wordDistance;
    return wordDistanceSum;
  }

  public List<Gap> getAdditions() {
    final List<Gap> additions = Lists.newArrayList();
    for (final Gap gap : _gaps) {
      if (gap.isAddition()) {
        additions.add(gap);
      }
    }
    return additions;
  }

  public List<Gap> getReplacements() {
    final List<Gap> replacements = Lists.newArrayList();
    for (final Gap gap : _gaps) {
      if (gap.isReplacement()) {
        replacements.add(gap);
      }
    }
    return replacements;
  }

  // TODO: rename; add s
  public Collection<Transposition> getTranpositions() {
    final List<Tuple2<MatchSequence<T>>> calculateSequenceTuples = TranspositionDetection.calculateSequenceTuples(getMatchSequencesOrderedForWitnessA(), getMatchSequencesOrderedForWitnessB());
    final List<Tuple2<MatchSequence<T>>> filterAwayRealMatches = TranspositionDetection.filterAwayRealMatches(calculateSequenceTuples);
    final List<Transposition> createTranspositions = TranspositionDetection.createTranspositions(filterAwayRealMatches);
    return createTranspositions;
  }

  public Alignment makeAddDelFromTrans(final Segment a, final Segment b) {
    // remove duplicates from transpositions
    final Stack<Transposition> transToCheck = new Stack<Transposition>();
    final List<Transposition> transpositions = Lists.newArrayList();
    transToCheck.addAll(getTranpositions());
    while (!transToCheck.isEmpty()) {
      final Transposition top = transToCheck.pop();
      transpositions.add(top);
      for (final Transposition tr : transToCheck) {
        if (tr.getBase().equals(top.getWitness())) {
          if (tr.getWitness().equals(top.getBase())) {
            transToCheck.remove(tr);
            break;
          }
        }
      }
    }
    // remove matches from transpositions
    final Set<Match<T>> matches = this.getMatches();
    final List<Gap> gaps = this.getGaps();
    //    System.out.println(transpositions);
    for (final Transposition t : transpositions) {
      final MatchSequence<Word> base = t.getBase();
      for (final Match<Word> match : base.getMatches()) {
        //        System.out.println("WHAT? " + match);
        matches.remove(match);
      }
      // make an addition from the matchSequence
      final Word w = (Word) t.getBase().getFirstMatch().getWitnessWord();
      final Word o = (Word) t.getBase().getLastMatch().getWitnessWord();
      //      System.out.println(w);
      //      System.out.println(o);
      final BaseContainerPart<Word> partNull = new BaseContainerPart<Word>(null, 0, 0, 0, null, null);
      final BaseContainerPart<Word> partAdd = new BaseContainerPart<Word>(b, w, o);
      final Gap addition = new Gap(partNull, partAdd, null);
      //      System.out.println(partAdd.hasGap());
      //      System.out.println(addition.isAddition());
      gaps.add(addition);
    }
    final Alignment<T> al = new Alignment<T>(matches, gaps, getMatchSequencesOrderedForWitnessA(), getMatchSequencesOrderedForWitnessB());
    return al;
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
