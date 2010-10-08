package eu.interedition.collatex2.alignmenttable;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.PairwiseAlignmentHelper;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableTranspositionTest {
  private static Logger logger = LoggerFactory.getLogger(AlignmentTableTranspositionTest.class);
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
  public void testDoubleTransposition3() {
    final IWitness a = engine.createWitness("A", "a b c");
    final IWitness b = engine.createWitness("B", "b a c");
    final IAlignmentTable alignmentTable = engine.align(a, b);
    final String expected = "A:  |a|b|c\n" + "B: b|a| |c\n";
    final String actual = alignmentTable.toString();
    Assert.assertEquals(expected, actual);
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
  
  // Test made by Gregor Middell
  @Test
  public void testSimpleTransposition() {
    final IWitness w1 = engine.createWitness("A", "A black cat in a white basket");
    final IWitness w2 = engine.createWitness("B", "A white cat in a black basket");
    final IAlignmentTable table = engine.align(w1, w2);
    logger.debug(table.toString());
    String expected = "A: a|black|cat|in|a|white|basket\n";
    expected += "B: a|white|cat|in|a|black|basket\n";
    Assert.assertEquals(expected, table.toString());
  }


}
