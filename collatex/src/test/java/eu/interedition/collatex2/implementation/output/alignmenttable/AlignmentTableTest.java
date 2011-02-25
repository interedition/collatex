/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.implementation.output.alignmenttable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setUp() {
    engine = new CollateXEngine();
  }

  @Test
  public void testEmptyGraph() {
    IAlignmentTable table = engine.align();
    assertEquals(0, table.getRows().size());
  }

  @Test
  public void testFirstWitness() {
    IWitness a = engine.createWitness("A", "the black cat");
    final IAlignmentTable table = engine.align(a);
    assertEquals("A: |the|black|cat|", table.getRow(a).rowToString());
    assertEquals(1, table.getRows().size());
  }
  
  @Test
  public void testEverythingMatches() {
    final IWitness a = engine.createWitness("A", "the black cat");
    final IWitness b = engine.createWitness("B", "the black cat");
    final IWitness c = engine.createWitness("C", "the black cat");
    final IAlignmentTable table = engine.align(a, b, c);
    assertEquals("A: |the|black|cat|", table.getRow(a).rowToString());
    assertEquals("B: |the|black|cat|", table.getRow(b).rowToString());
    assertEquals("C: |the|black|cat|", table.getRow(c).rowToString());
    assertEquals(3, table.getRows().size());
  }
  
  @Test
  public void testVariant() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the white cat");
    final IWitness w3 = engine.createWitness("C", "the green cat");
    final IWitness w4 = engine.createWitness("D", "the red cat");
    final IWitness w5 = engine.createWitness("E", "the yellow cat");
    final IAlignmentTable table = engine.align(w1, w2, w3, w4, w5);
    assertEquals("A: |the|black|cat|", table.getRow(w1).rowToString());
    assertEquals("B: |the|white|cat|", table.getRow(w2).rowToString());
    assertEquals("C: |the|green|cat|", table.getRow(w3).rowToString());
    assertEquals("D: |the|red|cat|", table.getRow(w4).rowToString());
    assertEquals("E: |the|yellow|cat|", table.getRow(w5).rowToString());
    assertEquals(5, table.getRows().size());
  }

  @Test
  public void testOmission() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the cat");
    final IWitness w3 = engine.createWitness("C", "the black cat");
    final IAlignmentTable table = engine.align(w1, w2, w3);
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
    final IAlignmentTable table = engine.align(w1, w2, w3, w4);
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
    final IAlignmentTable table = engine.align(w1, w2, w3, w4);
    String expected = "A:  | | |the| |cat| \n";
    expected += "B:  |before| |the| |cat| \n";
    expected += "C:  | | |the|black|cat| \n";
    expected += "D: just|before|midnight|the| |cat|walks\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testTranspositionAndReplacement() {
    final IWitness w1 = engine.createWitness("A", "The black dog chases a red cat.");
    final IWitness w2 = engine.createWitness("B", "A red cat chases the black dog.");
    final IWitness w3 = engine.createWitness("C", "A red cat chases the yellow dog");
    final IAlignmentTable table = engine.align(w1, w2, w3);
    String expected = "A: the|black|dog|chases|a|red|cat\n";
    expected += "B: a|red|cat|chases|the|black|dog\n";
    expected += "C: a|red|cat|chases|the|yellow|dog\n";
    assertEquals(expected, table.toString());
  }
  
  //NOTE: by default we align to the left!
  //NOTE: right alignment would be nicer in this specific case!
  //TODO: AI This one is tricky!
  @Ignore
  @Test
  public void testVariation() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the black and white cat");
    final IWitness w3 = engine.createWitness("C", "the black very special cat");
    final IWitness w4 = engine.createWitness("D", "the black not very special cat");
    final IAlignmentTable table = engine.align(w1, w2, w3, w4);
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
  
  @Test
  public void testSimpleSpencerHowe() {
    IWitness w1 = engine.createWitness("A", "a");
    IWitness w2 = engine.createWitness("B", "b");
    IWitness w3 = engine.createWitness("C", "a b");
    final IAlignmentTable table = engine.align(w1, w2, w3);
    assertEquals("A: |a| |", table.getRow(w1).rowToString());
    assertEquals("B: | |b|", table.getRow(w2).rowToString());
    assertEquals("C: |a|b|", table.getRow(w3).rowToString());
    assertEquals(3, table.getRows().size());
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
  public void testGetRow() {
    final IWitness w1 = engine.createWitness("A", "the black and white cat");
    final IWitness w2 = engine.createWitness("B", "the red cat");
    IAlignmentTable table = engine.align(w1, w2);
    IRow rowA = table.getRow(w1);
    Iterator<ICell> iteratorA = rowA.iterator();
    assertEquals("the", iteratorA.next().getToken().getNormalized());
    assertTrue(!iteratorA.next().isEmpty());
    IRow rowB = table.getRow(w2);
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
