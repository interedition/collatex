package eu.interedition.collatex2.alignmenttable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTable4;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setUp() {
    engine = new CollateXEngine();
  }

  @Test
  public void testFirstWitness() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IAlignmentTable table = engine.createAligner().add(w1).getResult();
    final String expected = "A: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testEverythingMatches() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the black cat");
    final IWitness w3 = engine.createWitness("C", "the black cat");
    final IAlignmentTable table = engine.createAligner().add(w1, w2, w3).getResult();
    String expected = "A: the|black|cat\n";
    expected += "B: the|black|cat\n";
    expected += "C: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testVariant() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the white cat");
    final IWitness w3 = engine.createWitness("C", "the green cat");
    final IWitness w4 = engine.createWitness("D", "the red cat");
    final IWitness w5 = engine.createWitness("E", "the yellow cat");
    final IAlignmentTable table = engine.createAligner().add(w1, w2, w3, w4, w5).getResult();
    String expected = "A: the|black|cat\n";
    expected += "B: the|white|cat\n";
    expected += "C: the|green|cat\n";
    expected += "D: the|red|cat\n";
    expected += "E: the|yellow|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testOmission() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the cat");
    final IWitness w3 = engine.createWitness("C", "the black cat");
    final IAlignmentTable table = engine.createAligner().add(w1, w2, w3).getResult();
    String expected = "A: the|black|cat\n";
    expected += "B: the| |cat\n";
    expected += "C: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testAddition1() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the white and black cat");
    final IAlignmentTable table = engine.align(w1, w2);
    String expected = "A: the| | |black|cat\n";
    expected += "B: the|white|and|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testAddition2() {
    final IWitness w1 = engine.createWitness("A", "the cat");
    final IWitness w2 = engine.createWitness("B", "before the cat");
    final IWitness w3 = engine.createWitness("C", "the black cat");
    final IWitness w4 = engine.createWitness("D", "the cat walks");
    final IAlignmentTable table = engine.createAligner().add(w1, w2, w3, w4).getResult();
    String expected = "A:  |the| |cat| \n";
    expected += "B: before|the| |cat| \n";
    expected += "C:  |the|black|cat| \n";
    expected += "D:  |the| |cat|walks\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testAddition3() {
    final IWitness w1 = engine.createWitness("A", "the cat");
    final IWitness w2 = engine.createWitness("B", "before the cat");
    final IWitness w3 = engine.createWitness("C", "the black cat");
    final IWitness w4 = engine.createWitness("D", "just before midnight the cat walks");
    final IAlignmentTable table = engine.createAligner().add(w1, w2, w3, w4).getResult();
    String expected = "A:  | | |the| |cat| \n";
    expected += "B:  |before| |the| |cat| \n";
    expected += "C:  | | |the|black|cat| \n";
    expected += "D: just|before|midnight|the| |cat|walks\n";

    assertEquals(expected, table.toString());
  }

  // TODO: rewrite test to use addAddition
  @Test
  public void testAddVariantBeforeColumnAndPositions() {
    final IWitness witness = engine.createWitness("A", "two before two after");
    IAlignmentTable table = engine.createAligner().add(witness).getResult();

    final IWitness temp = engine.createWitness("B", "in between");
    final IPhrase tobeadded = temp.createPhrase(1, 2);
    final IColumn column = table.getColumns().get(2);
    ((AlignmentTable4) table).addVariantBefore(column, tobeadded);

    final List<IColumn> columns = table.getColumns();
    Assert.assertEquals(1, columns.get(0).getPosition());
    Assert.assertEquals(2, columns.get(1).getPosition());
    Assert.assertEquals(3, columns.get(2).getPosition());
    Assert.assertEquals(4, columns.get(3).getPosition());
    Assert.assertEquals(5, columns.get(4).getPosition());
    Assert.assertEquals(6, columns.get(5).getPosition());
  }

  @Test
  public void testVariation() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the black and white cat");
    final IWitness w3 = engine.createWitness("C", "the black very special cat");
    final IWitness w4 = engine.createWitness("D", "the black not very special cat");
    final IAlignmentTable table = engine.createAligner().add(w1, w2, w3, w4).getResult();
    String expected = "A: the|black| | | |cat\n";
    expected += "B: the|black| |and|white|cat\n";
    expected += "C: the|black| |very|special|cat\n";
    expected += "D: the|black|not|very|special|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testWitnessReorder() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the black and white cat");
    final IWitness w3 = engine.createWitness("C", "the black not very special cat");
    final IWitness w4 = engine.createWitness("D", "the black very special cat");
    final IAlignmentTable table = engine.align(w1, w2, w3, w4);
    String expected = "A: the|black| | | |cat\n";
    expected += "B: the|black|and|white| |cat\n";
    expected += "C: the|black|not|very|special|cat\n";
    expected += "D: the|black| |very|special|cat\n";
    assertEquals(expected, table.toString());
  }

  // Note: tests toString method
  @Test
  public void testStringOutputOneWitness() {
    final IWitness a = engine.createWitness("A", "the black cat");
    final IAlignmentTable table = engine.align(a);
    final String expected = "A: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  // Note: tests toString method
  @Test
  public void testStringOutputTwoWitnesses() {
    final IWitness a = engine.createWitness("A", "the black cat");
    final IWitness b = engine.createWitness("B", "the black cat");
    final IAlignmentTable table = engine.align(a, b);
    String expected = "A: the|black|cat\n";
    expected += "B: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  // Note: tests toString method
  @Test
  public void testStringOutputEmptyCells() {
    final IWitness a = engine.createWitness("A", "the black cat");
    final IWitness b = engine.createWitness("B", "the");
    final IAlignmentTable table = engine.align(a, b);
    String expected = "A: the|black|cat\n";
    expected += "B: the| | \n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testRepeatingTokensWithOneWitness() {
    final IWitness witness = engine.createWitness("a", "a c a t g c a");
    final IAlignmentTable alignmentTable = engine.align(witness);
    final List<String> repeatingTokens = alignmentTable.findRepeatingTokens();
    assertEquals(2, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("c"));
    assertFalse(repeatingTokens.contains("t"));
    assertFalse(repeatingTokens.contains("g"));
  }

  @Test
  public void testRepeatingTokensWithMultipleWitnesses() {
    final IWitness witnessA = engine.createWitness("a", "a c a t g c a");
    final IWitness witnessB = engine.createWitness("b", "a c a t t c a");
    final IAlignmentTable alignmentTable = engine.align(witnessA, witnessB);
    final List<String> repeatingTokens = alignmentTable.findRepeatingTokens();
    assertEquals(3, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("c"));
    assertTrue(repeatingTokens.contains("t"));
    assertFalse(repeatingTokens.contains("g"));
  }

  @Test
  public void testRepeatingTokensWithMultipleWitnesses2() {
    final IWitness witnessA = engine.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = engine.createWitness("B", "this one very different");
    final IAlignmentTable alignmentTable = engine.align(witnessA, witnessB);
    final List<String> repeatingTokens = alignmentTable.findRepeatingTokens();
    assertEquals(0, repeatingTokens.size());
  }

  @Test
  public void testGetRow() {
    final IWitness w1 = engine.createWitness("A", "the black and white cat");
    final IWitness w2 = engine.createWitness("B", "the red cat");
    IAlignmentTable table = engine.align(w1, w2);
    IRow rowA = table.getRow("A");
    Iterator<ICell> iteratorA = rowA.iterator();
    assertEquals("the", iteratorA.next().getToken().getNormalized());
    assertEquals(2, iteratorA.next().getPosition());
    assertTrue(!iteratorA.next().isEmpty());
    IRow rowB = table.getRow("B");
    Iterator<ICell> iteratorB = rowB.iterator();
    assertEquals("the", iteratorB.next().getToken().getNormalized());
    assertEquals("red", iteratorB.next().getToken().getNormalized());
    assertTrue(iteratorB.next().isEmpty());
    assertTrue(iteratorB.next().isEmpty());
    assertEquals("cat", iteratorB.next().getToken().getNormalized());
    assertTrue(!iteratorB.hasNext());
  }
  
  @Test
  public void testGetRows() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "and white cat");
    final IWitness w3 = engine.createWitness("C", "the red cat");
    IAlignmentTable table = engine.align(w1, w2, w3);
    List<IRow> rows = table.getRows();
    assertEquals(3, rows.size());
    Iterator<IRow> iterator = rows.iterator();
    assertEquals("A", iterator.next().getSigil());
    assertEquals("B", iterator.next().getSigil());
    assertEquals("C", iterator.next().getSigil());
  }

}
