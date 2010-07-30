package com.sd_editions.collatex.permutations;

import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.alignment.functions.GapDetection;
import eu.interedition.collatex.alignment.functions.SequenceDetection;
import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.visualization.Visualization;

public class MatchSequenceTest extends TestCase {
  private static final String witnessId1 = "alpha";
  private static final String witnessId2 = "beta";
  private WitnessBuilder builder;

  @Override
  protected void setUp() throws Exception {
    builder = new WitnessBuilder();
    super.setUp();
  }

  public void testSimple() {
    final String base = "a b";
    final String witness = "a b";
    final CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    final List<MatchSequence<Word>> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (2->2)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testAddition() {
    final String base = "a b";
    final String witness = "a c b";
    final CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    final List<MatchSequence<Word>> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (2->3)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testOmission() {
    final String base = "a c b";
    final String witness = "a b";
    final CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    final List<MatchSequence<Word>> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (3->2)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testReplacement() {
    final String base = "a b c";
    final String witness = "a d c";
    final CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    final List<MatchSequence<Word>> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (3->3)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testTransposition() {
    final String base = "a b";
    final String witness = "b a";
    final CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    final List<MatchSequence<Word>> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->2)]", sequences.get(0).toString());
    assertEquals("[(2->1)]", sequences.get(1).toString());
    assertEquals(2, sequences.size());
  }

  //Note: this test should lead to a transposition :-)
  public void testComplex() {
    final String base = "The black dog chases a red cat.";
    final String witness = "A red cat chases the yellow dog";
    final CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    final List<MatchSequence<Word>> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->5), (3->7)]", sequences.get(0).toString());
    assertEquals("[(4->4)]", sequences.get(1).toString());
    assertEquals("[(5->1), (6->2), (7->3)]", sequences.get(2).toString());
    assertEquals(3, sequences.size());
  }

  public void testTranspositionExample1() {
    final CollateCore colors = new CollateCore(builder.buildWitnesses("a b c d e", "a c d b e"));
    final List<MatchSequence<Word>> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1)]", sequences.get(0).toString());
    assertEquals("[(2->4)]", sequences.get(1).toString());
    assertEquals("[(3->2), (4->3)]", sequences.get(2).toString());
    assertEquals("[(5->5)]", sequences.get(3).toString());
    assertEquals(4, sequences.size());
  }

  // abc de
  // de abc
  @SuppressWarnings("boxing")
  public void testSortSequencesForWitness() {
    final Match a = new Match(new Word(witnessId1, "A", 1), new Word(witnessId2, "A", 3));
    final Match b = new Match(new Word(witnessId1, "B", 2), new Word(witnessId2, "B", 4));
    final Match c = new Match(new Word(witnessId1, "C", 3), new Word(witnessId2, "C", 5));
    final MatchSequence<Word> sequence = new MatchSequence<Word>(1, a, b, c);
    final Match d = new Match(new Word(witnessId1, "D", 4), new Word(witnessId2, "D", 1));
    final Match e = new Match(new Word(witnessId1, "E", 5), new Word(witnessId2, "E", 2));
    final MatchSequence<Word> sequence2 = new MatchSequence<Word>(2, d, e);
    final List<MatchSequence<Word>> matchSequences = Lists.newArrayList(sequence, sequence2);
    final List<MatchSequence<Word>> arrayForWitness = SequenceDetection.sortSequencesForWitness(matchSequences);
    assertEquals(Lists.newArrayList(sequence2, sequence), arrayForWitness);
  }

  @SuppressWarnings("boxing")
  public void testConvertMatchSequencesToTuples() {
    final Match a = new Match(new Word(witnessId1, "A", 1), new Word(witnessId2, "A", 3));
    final Match b = new Match(new Word(witnessId1, "B", 2), new Word(witnessId2, "B", 4));
    final Match c = new Match(new Word(witnessId1, "C", 3), new Word(witnessId2, "C", 5));
    final MatchSequence<Word> sequence = new MatchSequence<Word>(1, a, b, c);
    final Match d = new Match(new Word(witnessId1, "D", 4), new Word(witnessId2, "D", 1));
    final Match e = new Match(new Word(witnessId1, "E", 5), new Word(witnessId2, "E", 2));
    final MatchSequence<Word> sequence2 = new MatchSequence<Word>(2, d, e);
    final List<MatchSequence<Word>> matchSequencesForBase = Lists.newArrayList(sequence, sequence2);
    final List<MatchSequence<Word>> matchSequencesForWitness = Lists.newArrayList(sequence2, sequence);
    final List<Tuple2<MatchSequence<Word>>> matchSequenceTuples = TranspositionDetection.calculateSequenceTuples(matchSequencesForBase, matchSequencesForWitness);
    final Tuple2<MatchSequence> expected1 = new Tuple2<MatchSequence>(sequence, sequence2, null);
    final Tuple2<MatchSequence> expected2 = new Tuple2<MatchSequence>(sequence2, sequence, null);
    assertEquals(expected1, matchSequenceTuples.get(0));
    assertEquals(expected2, matchSequenceTuples.get(1));
  }

  // A B C
  // B A C
  @SuppressWarnings( { "boxing", "unchecked" })
  public void testFilterAwayRealMatches() {
    final Match a = new Match(new Word(witnessId1, "A", 1), new Word(witnessId2, "A", 2));
    final Match b = new Match(new Word(witnessId1, "B", 2), new Word(witnessId2, "B", 1));
    final Match c = new Match(new Word(witnessId1, "C", 3), new Word(witnessId2, "C", 3));
    final MatchSequence sequenceA = new MatchSequence(1, a);
    final MatchSequence sequenceB = new MatchSequence(2, b);
    final MatchSequence sequenceC = new MatchSequence(3, c);
    final Tuple2<MatchSequence<Word>> tuple1 = new Tuple2<MatchSequence<Word>>(sequenceA, sequenceB, null);
    final Tuple2<MatchSequence<Word>> tuple2 = new Tuple2<MatchSequence<Word>>(sequenceB, sequenceA, null);
    final Tuple2<MatchSequence<Word>> tuple3 = new Tuple2<MatchSequence<Word>>(sequenceC, sequenceC, null);
    final List<Tuple2<MatchSequence<Word>>> tuples = Lists.newArrayList(tuple1, tuple2, tuple3);
    final List<Tuple2<MatchSequence<Word>>> expected = Lists.newArrayList(tuple1, tuple2);
    final List<Tuple2<MatchSequence<Word>>> actual = TranspositionDetection.filterAwayRealMatches(tuples);
    assertEquals(expected, actual);
  }

  // A B C
  // B A C
  @SuppressWarnings( { "boxing", "unchecked" })
  public void testGetRealMatches() {
    final Match a = new Match(new Word(witnessId1, "A", 1), new Word(witnessId2, "A", 2));
    final Match b = new Match(new Word(witnessId1, "B", 2), new Word(witnessId2, "B", 1));
    final Match c = new Match(new Word(witnessId1, "C", 3), new Word(witnessId2, "C", 3));
    final MatchSequence sequenceA = new MatchSequence(1, a);
    final MatchSequence sequenceB = new MatchSequence(2, b);
    final MatchSequence sequenceC = new MatchSequence(3, c);
    final Tuple2<MatchSequence> tuple1 = new Tuple2<MatchSequence>(sequenceA, sequenceB, null);
    final Tuple2<MatchSequence> tuple2 = new Tuple2<MatchSequence>(sequenceB, sequenceA, null);
    final Tuple2<MatchSequence> tuple3 = new Tuple2<MatchSequence>(sequenceC, sequenceC, null);
    final List<Tuple2<MatchSequence>> tuples = Lists.newArrayList(tuple1, tuple2, tuple3);
    final List<MatchSequence> expected = Lists.newArrayList(sequenceC);
    final List<MatchSequence> actual = TranspositionDetection.getMatches(tuples);
    assertEquals(expected, actual);
  }

  @SuppressWarnings("boxing")
  public void testNoModificationsInMatchSequences() {
    final Word aB = new Word(witnessId1, "A", 1);
    final Word bB = new Word(witnessId1, "B", 2);
    final Word aW = new Word(witnessId2, "A", 1);
    final Word bW = new Word(witnessId2, "B", 2);
    final Segment base = new Segment(aB, bB);
    final Segment witness = new Segment(aW, bW);
    final Match a = new Match(aB, aW);
    final Match b = new Match(bB, bW);
    final MatchSequence<Word> sequence = new MatchSequence(1, a, b);
    final List<MatchSequence<Word>> sequences = Lists.newArrayList(sequence);
    final List<Gap<Word>> variants = GapDetection.getVariantsInMatchSequences(base, witness, sequences);
    final List<Modification> results = Visualization.analyseVariants(variants);
    final List<Modification> modificationsInMatchSequences = results;
    assertEquals(0, modificationsInMatchSequences.size());
  }

  // TODO test gaps of more than 1 word?

}
