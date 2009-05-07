package com.sd_editions.collatex.permutations;

import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;

public class MatchSequenceTest extends TestCase {
  private final String witnessId1 = "alpha";
  private final String witnessId2 = "beta";
  private WitnessBuilder builder;

  @Override
  protected void setUp() throws Exception {
    builder = new WitnessBuilder();
    super.setUp();
  }

  public void testSimple() {
    String base = "a b";
    String witness = "a b";
    CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (2->2)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testAddition() {
    String base = "a b";
    String witness = "a c b";
    CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (2->3)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testOmission() {
    String base = "a c b";
    String witness = "a b";
    CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (3->2)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testReplacement() {
    String base = "a b c";
    String witness = "a d c";
    CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (3->3)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testTransposition() {
    String base = "a b";
    String witness = "b a";
    CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->2)]", sequences.get(0).toString());
    assertEquals("[(2->1)]", sequences.get(1).toString());
    assertEquals(2, sequences.size());
  }

  //Note: this test should lead to a transposition :-)
  public void testComplex() {
    String base = "The black dog chases a red cat.";
    String witness = "A red cat chases the yellow dog";
    CollateCore colors = new CollateCore(builder.buildWitnesses(new String[] { base, witness }));
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->5), (3->7)]", sequences.get(0).toString());
    assertEquals("[(4->4)]", sequences.get(1).toString());
    assertEquals("[(5->1), (6->2), (7->3)]", sequences.get(2).toString());
    assertEquals(3, sequences.size());
  }

  public void testTranspositionExample1() {
    CollateCore colors = new CollateCore(builder.buildWitnesses("a b c d e", "a c d b e"));
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
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
    Match a = new Match(new Word(witnessId1, "A", 1), new Word(witnessId2, "A", 3));
    Match b = new Match(new Word(witnessId1, "B", 2), new Word(witnessId2, "B", 4));
    Match c = new Match(new Word(witnessId1, "C", 3), new Word(witnessId2, "C", 5));
    MatchSequence sequence = new MatchSequence(1, a, b, c);
    Match d = new Match(new Word(witnessId1, "D", 4), new Word(witnessId2, "D", 1));
    Match e = new Match(new Word(witnessId1, "E", 5), new Word(witnessId2, "E", 2));
    MatchSequence sequence2 = new MatchSequence(2, d, e);
    List<MatchSequence> matchSequences = Lists.newArrayList(sequence, sequence2);
    List<MatchSequence> arrayForWitness = SequenceDetection.sortSequencesForWitness(matchSequences);
    assertEquals(Lists.newArrayList(sequence2, sequence), arrayForWitness);
  }

  @SuppressWarnings("boxing")
  public void testConvertMatchSequencesToTuples() {
    Match a = new Match(new Word(witnessId1, "A", 1), new Word(witnessId2, "A", 3));
    Match b = new Match(new Word(witnessId1, "B", 2), new Word(witnessId2, "B", 4));
    Match c = new Match(new Word(witnessId1, "C", 3), new Word(witnessId2, "C", 5));
    MatchSequence sequence = new MatchSequence(1, a, b, c);
    Match d = new Match(new Word(witnessId1, "D", 4), new Word(witnessId2, "D", 1));
    Match e = new Match(new Word(witnessId1, "E", 5), new Word(witnessId2, "E", 2));
    MatchSequence sequence2 = new MatchSequence(2, d, e);
    List<MatchSequence> matchSequencesForBase = Lists.newArrayList(sequence, sequence2);
    List<MatchSequence> matchSequencesForWitness = Lists.newArrayList(sequence2, sequence);
    List<Tuple2<MatchSequence>> matchSequenceTuples = TranspositionDetection.calculateSequenceTuples(matchSequencesForBase, matchSequencesForWitness);
    Tuple2<MatchSequence> expected1 = new Tuple2<MatchSequence>(sequence, sequence2);
    Tuple2<MatchSequence> expected2 = new Tuple2<MatchSequence>(sequence2, sequence);
    assertEquals(expected1, matchSequenceTuples.get(0));
    assertEquals(expected2, matchSequenceTuples.get(1));
  }

  // A B C
  // B A C
  @SuppressWarnings( { "boxing", "unchecked" })
  public void testFilterAwayRealMatches() {
    Match a = new Match(new Word(witnessId1, "A", 1), new Word(witnessId2, "A", 2));
    Match b = new Match(new Word(witnessId1, "B", 2), new Word(witnessId2, "B", 1));
    Match c = new Match(new Word(witnessId1, "C", 3), new Word(witnessId2, "C", 3));
    MatchSequence sequenceA = new MatchSequence(1, a);
    MatchSequence sequenceB = new MatchSequence(2, b);
    MatchSequence sequenceC = new MatchSequence(3, c);
    Tuple2<MatchSequence> tuple1 = new Tuple2<MatchSequence>(sequenceA, sequenceB);
    Tuple2<MatchSequence> tuple2 = new Tuple2<MatchSequence>(sequenceB, sequenceA);
    Tuple2<MatchSequence> tuple3 = new Tuple2<MatchSequence>(sequenceC, sequenceC);
    List<Tuple2<MatchSequence>> tuples = Lists.newArrayList(tuple1, tuple2, tuple3);
    List<Tuple2<MatchSequence>> expected = Lists.newArrayList(tuple1, tuple2);
    List<Tuple2<MatchSequence>> actual = TranspositionDetection.filterAwayRealMatches(tuples);
    assertEquals(expected, actual);
  }

  // A B C
  // B A C
  @SuppressWarnings( { "boxing", "unchecked" })
  public void testGetRealMatches() {
    Match a = new Match(new Word(witnessId1, "A", 1), new Word(witnessId2, "A", 2));
    Match b = new Match(new Word(witnessId1, "B", 2), new Word(witnessId2, "B", 1));
    Match c = new Match(new Word(witnessId1, "C", 3), new Word(witnessId2, "C", 3));
    MatchSequence sequenceA = new MatchSequence(1, a);
    MatchSequence sequenceB = new MatchSequence(2, b);
    MatchSequence sequenceC = new MatchSequence(3, c);
    Tuple2<MatchSequence> tuple1 = new Tuple2<MatchSequence>(sequenceA, sequenceB);
    Tuple2<MatchSequence> tuple2 = new Tuple2<MatchSequence>(sequenceB, sequenceA);
    Tuple2<MatchSequence> tuple3 = new Tuple2<MatchSequence>(sequenceC, sequenceC);
    List<Tuple2<MatchSequence>> tuples = Lists.newArrayList(tuple1, tuple2, tuple3);
    List<MatchSequence> expected = Lists.newArrayList(sequenceC);
    List<MatchSequence> actual = TranspositionDetection.getMatches(tuples);
    assertEquals(expected, actual);
  }

  // a b
  // a c b
  @SuppressWarnings("boxing")
  public void testModificationsInMatchSequences() {
    Word aB = new Word(witnessId1, "A", 1);
    Word bB = new Word(witnessId1, "B", 2);
    Word aW = new Word(witnessId2, "A", 1);
    Word cW = new Word(witnessId2, "C", 2);
    Word bW = new Word(witnessId2, "B", 3);
    Witness base = new Witness(aB, bB);
    Witness witness = new Witness(aW, cW, bW);
    Match a = new Match(aB, aW);
    Match b = new Match(bB, bW);
    MatchSequence sequence = new MatchSequence(1, a, b);
    List<MatchSequence> sequences = Lists.newArrayList(sequence);
    List<MisMatch> variants = MatchSequences.getVariantsInMatchSequences(base, witness, sequences);
    List<Modification> results = MatchSequences.analyseVariants(variants);
    List<Modification> modificationsInMatchSequences = results;
    assertEquals(1, modificationsInMatchSequences.size());
    assertEquals("addition: C position: 2", modificationsInMatchSequences.get(0).toString());
  }

  @SuppressWarnings("boxing")
  public void testNoModificationsInMatchSequences() {
    Word aB = new Word(witnessId1, "A", 1);
    Word bB = new Word(witnessId1, "B", 2);
    Word aW = new Word(witnessId2, "A", 1);
    Word bW = new Word(witnessId2, "B", 2);
    Witness base = new Witness(aB, bB);
    Witness witness = new Witness(aW, bW);
    Match a = new Match(aB, aW);
    Match b = new Match(bB, bW);
    MatchSequence sequence = new MatchSequence(1, a, b);
    List<MatchSequence> sequences = Lists.newArrayList(sequence);
    List<MisMatch> variants = MatchSequences.getVariantsInMatchSequences(base, witness, sequences);
    List<Modification> results = MatchSequences.analyseVariants(variants);
    List<Modification> modificationsInMatchSequences = results;
    assertEquals(0, modificationsInMatchSequences.size());
  }

  public void testModificationsInBetweenMatchSequences() {
    CollateCore colors = new CollateCore(builder.buildWitnesses("a b y c z d", "a x b c n d"));
    Modifications compareWitness = colors.compareWitness(1, 2).get(0);
    assertEquals(3, compareWitness.size());
    assertEquals("addition: x position: 2", compareWitness.get(0).toString());
    assertEquals("omission: y position: 3", compareWitness.get(1).toString());
    assertEquals("replacement: z / n position: 5", compareWitness.get(2).toString());
  }

  public void testModificationAtTheEnd() {
    CollateCore colors = new CollateCore(builder.buildWitnesses("a b", "a c"));
    Modifications compareWitness = colors.compareWitness(1, 2).get(0);
    assertEquals(1, compareWitness.size());
    assertEquals("replacement: b / c position: 2", compareWitness.get(0).toString());
  }

  // TODO: test gaps of more than 1 word?

}
