package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;

public class MatchSequenceTest extends TestCase {
  public void testSimple() {
    String base = "a b";
    String witness = "a b";
    Colors colors = new Colors(base, witness);
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (2->2)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testAddition() {
    String base = "a b";
    String witness = "a c b";
    Colors colors = new Colors(base, witness);
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (2->3)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testOmission() {
    String base = "a c b";
    String witness = "a b";
    Colors colors = new Colors(base, witness);
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (3->2)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testReplacement() {
    String base = "a b c";
    String witness = "a d c";
    Colors colors = new Colors(base, witness);
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->1), (3->3)]", sequences.get(0).toString());
    assertEquals(1, sequences.size());
  }

  public void testTransposition() {
    String base = "a b";
    String witness = "b a";
    Colors colors = new Colors(base, witness);
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->2)]", sequences.get(0).toString());
    assertEquals("[(2->1)]", sequences.get(1).toString());
    assertEquals(2, sequences.size());
  }

  //Note: this test should lead to a transposition :-)
  public void testComplex() {
    String base = "The black dog chases a red cat.";
    String witness = "A red cat chases the yellow dog";
    Colors colors = new Colors(base, witness);
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->5), (3->7)]", sequences.get(0).toString());
    assertEquals("[(4->4)]", sequences.get(1).toString());
    assertEquals("[(5->1), (6->2), (7->3)]", sequences.get(2).toString());
    assertEquals(3, sequences.size());
  }

  public void testTranspositionExample1() {
    Colors colors = new Colors("a b c d e", "a c d b e");
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
    Match a = new Match(new Word("A", 1), new Word("A", 3), 1);
    Match b = new Match(new Word("B", 2), new Word("B", 4), 2);
    Match c = new Match(new Word("C", 3), new Word("C", 5), 3);
    MatchSequence sequence = new MatchSequence(a, b, c);
    Match d = new Match(new Word("D", 4), new Word("D", 1), 4);
    Match e = new Match(new Word("E", 5), new Word("E", 2), 5);
    MatchSequence sequence2 = new MatchSequence(d, e);
    List<MatchSequence> matchSequences = Lists.newArrayList(sequence, sequence2);
    List<MatchSequence> arrayForWitness = TranspositionDetection.sortSequencesForWitness(matchSequences);
    assertEquals(Lists.newArrayList(sequence2, sequence), arrayForWitness);
  }

  @SuppressWarnings("boxing")
  public void testConvertMatchSequencesToTuples() {
    Match a = new Match(new Word("A", 1), new Word("A", 3), 1);
    Match b = new Match(new Word("B", 2), new Word("B", 4), 2);
    Match c = new Match(new Word("C", 3), new Word("C", 5), 3);
    MatchSequence sequence = new MatchSequence(a, b, c);
    Match d = new Match(new Word("D", 4), new Word("D", 1), 4);
    Match e = new Match(new Word("E", 5), new Word("E", 2), 5);
    MatchSequence sequence2 = new MatchSequence(d, e);
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
    Match a = new Match(new Word("A", 1), new Word("A", 2), 1);
    Match b = new Match(new Word("B", 2), new Word("B", 1), 2);
    Match c = new Match(new Word("C", 3), new Word("C", 3), 3);
    MatchSequence sequenceA = new MatchSequence(a);
    MatchSequence sequenceB = new MatchSequence(b);
    MatchSequence sequenceC = new MatchSequence(c);
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
    Match a = new Match(new Word("A", 1), new Word("A", 2), 1);
    Match b = new Match(new Word("B", 2), new Word("B", 1), 2);
    Match c = new Match(new Word("C", 3), new Word("C", 3), 3);
    MatchSequence sequenceA = new MatchSequence(a);
    MatchSequence sequenceB = new MatchSequence(b);
    MatchSequence sequenceC = new MatchSequence(c);
    Tuple2<MatchSequence> tuple1 = new Tuple2<MatchSequence>(sequenceA, sequenceB);
    Tuple2<MatchSequence> tuple2 = new Tuple2<MatchSequence>(sequenceB, sequenceA);
    Tuple2<MatchSequence> tuple3 = new Tuple2<MatchSequence>(sequenceC, sequenceC);
    List<Tuple2<MatchSequence>> tuples = Lists.newArrayList(tuple1, tuple2, tuple3);
    List<MatchSequence> expected = Lists.newArrayList(sequenceC);
    List<MatchSequence> actual = TranspositionDetection.getMatches(tuples);
    assertEquals(expected, actual);
  }

}
