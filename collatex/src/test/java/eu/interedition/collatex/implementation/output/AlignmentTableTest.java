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

package eu.interedition.collatex.implementation.output;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlignmentTableTest extends AbstractTest {

  @Test
  public void emptyTable() {
    assertEquals(0, align(merge(createWitnesses())).getRows().size());
  }

  @Test
  public void firstWitness() {
    final IWitness[] w = createWitnesses("the black cat");
    final AlignmentTable table = align(w);
    assertEquals(1, table.getRows().size());
    assertEquals("A: |the|black|cat|", table.getRow(w[0]).toString());
  }
  
  @Test
  public void everythingMatches() {
    final IWitness[] w = createWitnesses("the black cat", "the black cat", "the black cat");
    final AlignmentTable table = align(w);
    assertEquals(3, table.getRows().size());
    assertEquals("A: |the|black|cat|", table.getRow(w[0]).toString());
    assertEquals("B: |the|black|cat|", table.getRow(w[1]).toString());
    assertEquals("C: |the|black|cat|", table.getRow(w[2]).toString());
  }
  
  @Test
  public void variant() {
    final IWitness[] w = createWitnesses("the black cat", "the white cat", "the green cat", "the red cat", "the yellow cat");
    final AlignmentTable table = align(w);
    assertEquals(5, table.getRows().size());
    assertEquals("A: |the|black|cat|", table.getRow(w[0]).toString());
    assertEquals("B: |the|white|cat|", table.getRow(w[1]).toString());
    assertEquals("C: |the|green|cat|", table.getRow(w[2]).toString());
    assertEquals("D: |the|red|cat|", table.getRow(w[3]).toString());
    assertEquals("E: |the|yellow|cat|", table.getRow(w[4]).toString());
  }

  @Test
  public void omission() {
    final AlignmentTable table = align("the black cat", "the cat", "the black cat");
    assertEquals("A: the|black|cat\nB: the| |cat\nC: the|black|cat\n", table.toString());
  }

  @Test
  public void addition1() {
    final AlignmentTable table = align("the black cat", "the white and black cat");
    assertEquals("A: the| | |black|cat\nB: the|white|and|black|cat\n", table.toString());
  }

  @Test
  public void addition2() {
    final AlignmentTable table = align("the cat", "before the cat", "the black cat", "the cat walks");
    assertEquals("A:  |the| |cat| \nB: before|the| |cat| \nC:  |the|black|cat| \nD:  |the| |cat|walks\n", table.toString());
  }

  @Test
  public void addition3() {
    final AlignmentTable t = align("the cat", "before the cat", "the black cat", "just before midnight the cat walks");
    assertEquals("A:  | | |the| |cat| \nB:  |before| |the| |cat| \nC:  | | |the|black|cat| \nD: just|before|midnight|the| |cat|walks\n", t.toString());
  }

  @Test
  public void transpositionAndReplacement() {
    final AlignmentTable t = align("The black dog chases a red cat.", "A red cat chases the black dog.", "A red cat chases the yellow dog");
    assertEquals("A: the|black|dog|chases|a|red|cat\nB: a|red|cat|chases|the|black|dog\nC: a|red|cat|chases|the|yellow|dog\n", t.toString());
  }
  
  @Test
  @Ignore("By default we align to the left; right alignment would be nicer in this specific case")
  public void variation() {
    final AlignmentTable t = align("the black cat", "the black and white cat", "the black very special cat", "the black not very special cat");
    assertEquals("A: the|black| | | |cat\nB: the|black| |and|white|cat\nC: the|black| |very|special|cat\nD: the|black|not|very|special|cat\n", t.toString());
  }

  @Test
  public void witnessReorder() {
    final AlignmentTable t = align("the black cat", "the black and white cat", "the black not very special cat", "the black very special cat");
    assertEquals("A: the|black| | | |cat\nB: the|black|and|white| |cat\nC: the|black|not|very|special|cat\nD: the|black| |very|special|cat\n", t.toString());
  }
  
  @Test
  public void testSimpleSpencerHowe() {
    final IWitness[] w = createWitnesses("a", "b", "a b");
    final AlignmentTable table = align(w);
    assertEquals(3, table.getRows().size());
    assertEquals("A: |a| |", table.getRow(w[0]).toString());
    assertEquals("B: | |b|", table.getRow(w[1]).toString());
    assertEquals("C: |a|b|", table.getRow(w[2]).toString());
  }
  


  @Test
  public void stringOutputOneWitness() {
    assertEquals("A: the|black|cat\n", align("the black cat").toString());
  }

  @Test
  public void stringOutputTwoWitnesses() {
    final AlignmentTable table = align("the black cat", "the black cat");
    assertEquals("A: the|black|cat\nB: the|black|cat\n", table.toString());
  }

  @Test
  public void stringOutputEmptyCells() {
    assertEquals("A: the|black|cat\nB: the| | \n", align("the black cat", "the").toString());
  }

  @Test
  public void getRow() {
    final IWitness[] w = createWitnesses("the black and white cat", "the red cat");
    final AlignmentTable table = align(w);

    final Iterator<Cell> iteratorA = table.getRow(w[0]).iterator();
    assertEquals("the", iteratorA.next().getToken().getNormalized());
    assertTrue(!iteratorA.next().isEmpty());

    final Iterator<Cell> iteratorB = table.getRow(w[1]).iterator();
    assertEquals("the", iteratorB.next().getToken().getNormalized());
    assertEquals("red", iteratorB.next().getToken().getNormalized());
    assertTrue(iteratorB.next().isEmpty());
    assertTrue(iteratorB.next().isEmpty());
    assertEquals("cat", iteratorB.next().getToken().getNormalized());
    assertTrue(!iteratorB.hasNext());
  }
  
  @Test
  public void getRows() {
    final List<Row> rows = align("the black cat", "and white cat", "the red cat").getRows();
    assertEquals(3, rows.size());

    final Iterator<Row> iterator = rows.iterator();
    assertEquals("A", iterator.next().getSigil());
    assertEquals("B", iterator.next().getSigil());
    assertEquals("C", iterator.next().getSigil());
  }

}
