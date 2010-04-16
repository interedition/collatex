package eu.interedition.collatex2.alignmenttable;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.PairwiseAlignmentHelper;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableTranspositionTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  //Note: this is more of an alignment test.. no table is involved here! 
  @Test
  public void testNoTransposition() {
    final IWitness a = engine.createWitness("A", "no transposition");
    final IWitness b = engine.createWitness("B", "no transposition");
    final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
    Assert.assertTrue(al.getTranspositions().isEmpty());
  }

  //Note: this is more of an alignment test.. no table is involved here! 
  @Test
  public void testNoTransposition2() {
    final IWitness a = engine.createWitness("A", "a b");
    final IWitness b = engine.createWitness("B", "c a");
    final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
    Assert.assertTrue(al.getTranspositions().isEmpty());
  }

  //Note: this is more of an alignment test.. no table is involved here! 
  @Test
  public void testDoubleTransposition() {
    final IWitness a = engine.createWitness("A", "a b");
    final IWitness b = engine.createWitness("B", "b a");
    final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
    Assert.assertEquals(2, al.getTranspositions().size());
    // 1: a -> b
    // 2: b -> a
  }

  //Note: this is more of an alignment test.. no table is involved here! 
  @Test
  public void testMultipleTransposition() {
    final IWitness a = engine.createWitness("A", "a b c");
    final IWitness b = engine.createWitness("B", "b c a");
    final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
    Assert.assertEquals(2, al.getTranspositions().size());
    // 1: a -> b c
    // 2: b c -> a
  }

  @Test
  public void testDoubleTransposition2() {
    final IWitness a = engine.createWitness("A", "a b");
    final IWitness b = engine.createWitness("B", "b a");
    final IAlignmentTable alignmentTable = engine.align(a, b);
    final String expected = "A:  |a|b\n" + "B: b|a| \n";
    final String actual = alignmentTable.toString();
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testDoubleTransposition3() {
    final IWitness a = engine.createWitness("A", "a b c");
    final IWitness b = engine.createWitness("B", "b a c");
    final IAlignmentTable alignmentTable = engine.align(a, b);
    final String expected = "A:  |a|b|c\n" + "B: b|a| |c\n";
    final String actual = alignmentTable.toString();
    Assert.assertEquals(expected, actual);
  }

  // TODO: rename test: mirrored transpositions with match in between!
  @Test
  public void testTranspositionsAreStoredInAlignmentTable() {
    final IWitness a = engine.createWitness("A", "the black and white cat");
    final IWitness b = engine.createWitness("B", "the white and black cat");
    final IAlignmentTable alignmentTable = engine.align(a, b);
    String expected = "A: the|black|and|white|cat\n";
    expected += "B: the|white|and|black|cat\n";
    assertEquals(expected, alignmentTable.toString());
  }

  @Test
  public void testTransposition() {
    final IWitness a = engine.createWitness("A", "the cat is black");
    final IWitness b = engine.createWitness("B", "black is the cat");
    final IAlignmentTable table = engine.align(a, b);
    String expected;
    expected = "A: the|cat|is|black| \n";
    expected += "B: black| |is|the|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testAdditionInCombinationWithTransposition() {
    final IWitness a = engine.createWitness("A", "the cat is very happy");
    final IWitness b = engine.createWitness("B", "very happy is the cat");
    final IWitness c = engine.createWitness("C", "very delitied and happy is the cat");
    final IAlignmentTable table = engine.align(a, b, c);
    String expected;
    expected = "A: the| | |cat|is|very|happy\n";
    expected += "B: very| | |happy|is|the|cat\n";
    expected += "C: very|delitied|and|happy|is|the|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testAdditionInCombinationWithTransposition2() {
    final IWitness a = engine.createWitness("A", "the cat is black");
    final IWitness b = engine.createWitness("B", "black is the cat");
    final IWitness c = engine.createWitness("C", "black and white is the cat");
    final IAlignmentTable table = engine.align(a, b, c);
    String expected;
    expected = "A: the|cat| |is|black| \n";
    expected += "B: black| | |is|the|cat\n";
    expected += "C: black|and|white|is|the|cat\n";
    assertEquals(expected, table.toString());
  }

}
