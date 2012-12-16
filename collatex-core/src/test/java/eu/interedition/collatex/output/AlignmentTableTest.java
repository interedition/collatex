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

package eu.interedition.collatex.output;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import eu.interedition.collatex.VariantGraph;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.RowSortedTable;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleWitness;

public class AlignmentTableTest extends AbstractTest {

  @Test
  public void emptyTable() {
    assertEquals(0, collate(createWitnesses()).toTable().size());
  }

  @Test
  public void firstWitness() {
    final SimpleWitness[] w = createWitnesses("the black cat");
    final RowSortedTable<Integer, Witness, Set<Token>> table = collate(w).toTable();
    assertEquals(1, table.columnKeySet().size());
    assertEquals("|the|black|cat|", toString(table, w[0]));
  }
  
  @Test
  public void everythingMatches() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the black cat", "the black cat");
    final RowSortedTable<Integer, Witness, Set<Token>> table = collate(w).toTable();
    assertEquals(3, table.columnKeySet().size());
    assertEquals("|the|black|cat|", toString(table, w[0]));
    assertEquals("|the|black|cat|", toString(table, w[1]));
    assertEquals("|the|black|cat|", toString(table, w[2]));
  }
  
  @Test
  public void variant() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the white cat", "the green cat", "the red cat", "the yellow cat");
    final RowSortedTable<Integer, Witness, Set<Token>> table = collate(w).toTable();
    assertEquals(5, table.columnKeySet().size());
    assertEquals("|the|black|cat|", toString(table, w[0]));
    assertEquals("|the|white|cat|", toString(table, w[1]));
    assertEquals("|the|green|cat|", toString(table, w[2]));
    assertEquals("|the|red|cat|", toString(table, w[3]));
    assertEquals("|the|yellow|cat|", toString(table, w[4]));
  }

  @Test
  public void omission() {
    final RowSortedTable<Integer, Witness, Set<Token>> table = collate("the black cat", "the cat", "the black cat").toTable();
    assertEquals("A: |the|black|cat|\nB: |the| |cat|\nC: |the|black|cat|\n", toString(table));
  }

  @Test
  public void addition1() {
    final RowSortedTable<Integer, Witness, Set<Token>> table = collate("the black cat", "the white and black cat").toTable();
    assertEquals("A: |the| | |black|cat|\nB: |the|white|and|black|cat|\n", toString(table));
  }

  @Test
  public void addition2() {
    final RowSortedTable<Integer, Witness, Set<Token>> table = collate("the cat", "before the cat", "the black cat", "the cat walks").toTable();
    assertEquals("A: | |the| |cat| |\nB: |before|the| |cat| |\nC: | |the|black|cat| |\nD: | |the| |cat|walks|\n", toString(table));
  }

  @Test
  public void addition3() {
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate("the cat", "before the cat", "the black cat", "just before midnight the cat walks").toTable();
    assertEquals("A: | | | |the| |cat| |\nB: | |before| |the| |cat| |\nC: | | | |the|black|cat| |\nD: |just|before|midnight|the| |cat|walks|\n", toString(t));
  }

  @Test
  public void transpositionAndReplacement() {
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate("the black dog chases a red cat", "a red cat chases the black dog", "a red cat chases the yellow dog").toTable();
    assertEquals("A: |the|black|dog|chases|a|red|cat|\nB: |a|red|cat|chases|the|black|dog|\nC: |a|red|cat|chases|the|yellow|dog|\n", toString(t));
  }
  
  @Test
  @Ignore("By default we align to the left; right alignment would be nicer in this specific case")
  public void variation() {
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate("the black cat", "the black and white cat", "the black very special cat", "the black not very special cat").toTable();
    assertEquals("A: |the|black| | | |cat|\nB: |the|black| |and|white|cat|\nC: |the|black| |very|special|cat|\nD: |the|black|not|very|special|cat|\n", toString(t));
  }

  @Test
  public void witnessReorder() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the black and white cat", "the black not very special cat", "the black very special cat");
    VariantGraph variantgraph = collate(w);
    final RowSortedTable<Integer, Witness, Set<Token>> table = variantgraph.toTable();
    assertEquals("|the|black| | | |cat|", toString(table, w[0]));
    assertEquals("|the|black|and|white| |cat|", toString(table, w[1]));
    assertEquals("|the|black|not|very|special|cat|", toString(table, w[2]));
    assertEquals("|the|black| |very|special|cat|", toString(table, w[3]));
  }
  
  @Test
  public void testSimpleSpencerHowe() {
    final SimpleWitness[] w = createWitnesses("a", "b", "a b");
    final RowSortedTable<Integer, Witness, Set<Token>> table = collate(w).toTable();
    assertEquals(3, table.columnKeySet().size());
    assertEquals("|a| |", toString(table, w[0]));
    assertEquals("| |b|", toString(table, w[1]));
    assertEquals("|a|b|", toString(table, w[2]));
  }

  @Test
  public void stringOutputOneWitness() {
    assertEquals("A: |the|black|cat|\n", toString(collate("the black cat").toTable()));
  }

  @Test
  public void stringOutputTwoWitnesses() {
    final RowSortedTable<Integer, Witness, Set<Token>> table = collate("the black cat", "the black cat").toTable();
    assertEquals("A: |the|black|cat|\nB: |the|black|cat|\n", toString(table));
  }

  @Test
  public void stringOutputEmptyCells() {
    assertEquals("A: |the|black|cat|\nB: |the| | |\n", toString(collate("the black cat", "the").toTable()));
  }
}
