/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.output;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

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
    assertEquals(0, table(collate(createWitnesses())).size());
  }

  @Test
  public void firstWitness() {
    final SimpleWitness[] w = createWitnesses("the black cat");
    final List<SortedMap<Witness, Set<Token>>> table = table(collate(w));
    assertEquals(1, witnesses(table).count());
    assertEquals("|the|black|cat|", toString(table, w[0]));
  }
  
  @Test
  public void everythingMatches() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the black cat", "the black cat");
    final List<SortedMap<Witness, Set<Token>>> table = table(collate(w));
    assertEquals(3, witnesses(table).count());
    assertEquals("|the|black|cat|", toString(table, w[0]));
    assertEquals("|the|black|cat|", toString(table, w[1]));
    assertEquals("|the|black|cat|", toString(table, w[2]));
  }
  
  @Test
  public void variant() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the white cat", "the green cat", "the red cat", "the yellow cat");
    final List<SortedMap<Witness, Set<Token>>> table = table(collate(w));
    assertEquals(5, witnesses(table).count());
    assertEquals("|the|black|cat|", toString(table, w[0]));
    assertEquals("|the|white|cat|", toString(table, w[1]));
    assertEquals("|the|green|cat|", toString(table, w[2]));
    assertEquals("|the|red|cat|", toString(table, w[3]));
    assertEquals("|the|yellow|cat|", toString(table, w[4]));
  }

  @Test
  public void omission() {
    final List<SortedMap<Witness, Set<Token>>> table = table(collate("the black cat", "the cat", "the black cat"));
    assertEquals("A: |the|black|cat|\nB: |the| |cat|\nC: |the|black|cat|\n", toString(table));
  }

  @Test
  public void addition1() {
    final List<SortedMap<Witness, Set<Token>>> table = table(collate("the black cat", "the white and black cat"));
    assertEquals("A: |the| | |black|cat|\nB: |the|white|and|black|cat|\n", toString(table));
  }

  @Test
  public void addition2() {
    final List<SortedMap<Witness, Set<Token>>> table = table(collate("the cat", "before the cat", "the black cat", "the cat walks"));
    assertEquals("A: | |the| |cat| |\nB: |before|the| |cat| |\nC: | |the|black|cat| |\nD: | |the| |cat|walks|\n", toString(table));
  }

  @Test
  public void addition3() {
    final List<SortedMap<Witness, Set<Token>>> t = table(collate("the cat", "before the cat", "the black cat", "just before midnight the cat walks"));
    assertEquals("A: | | | |the| |cat| |\nB: | |before| |the| |cat| |\nC: | | | |the|black|cat| |\nD: |just|before|midnight|the| |cat|walks|\n", toString(t));
  }

  @Test
  public void transpositionAndReplacement() {
    final
    List<SortedMap<Witness, Set<Token>>> t = table(collate("the black dog chases a red cat", "a red cat chases the black dog", "a red cat chases the yellow dog"));
    assertEquals("A: |the|black|dog|chases|a|red|cat|\nB: |a|red|cat|chases|the|black|dog|\nC: |a|red|cat|chases|the|yellow|dog|\n", toString(t));
  }
  
  @Test
  @Ignore("By default we align to the left; right alignment would be nicer in this specific case")
  public void variation() {
    final List<SortedMap<Witness, Set<Token>>> t = table(collate("the black cat", "the black and white cat", "the black very special cat", "the black not very special cat"));
    assertEquals("A: |the|black| | | |cat|\nB: |the|black| |and|white|cat|\nC: |the|black| |very|special|cat|\nD: |the|black|not|very|special|cat|\n", toString(t));
  }

  @Test
  public void witnessReorder() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the black and white cat", "the black not very special cat", "the black very special cat");
    VariantGraph variantgraph = collate(w);
    final List<SortedMap<Witness, Set<Token>>> table = table(variantgraph);
    assertEquals("|the|black| | | |cat|", toString(table, w[0]));
    assertEquals("|the|black|and|white| |cat|", toString(table, w[1]));
    assertEquals("|the|black|not|very|special|cat|", toString(table, w[2]));
    assertEquals("|the|black| |very|special|cat|", toString(table, w[3]));
  }
  
  @Test
  public void testSimpleSpencerHowe() {
    final SimpleWitness[] w = createWitnesses("a", "b", "a b");
    final List<SortedMap<Witness, Set<Token>>> table = table(collate(w));
    assertEquals(3, witnesses(table).count());
    assertEquals("|a| |", toString(table, w[0]));
    assertEquals("| |b|", toString(table, w[1]));
    assertEquals("|a|b|", toString(table, w[2]));
  }

  @Test
  public void stringOutputOneWitness() {
    assertEquals("A: |the|black|cat|\n", toString(table(collate("the black cat"))));
  }

  @Test
  public void stringOutputTwoWitnesses() {
    final List<SortedMap<Witness, Set<Token>>> table = table(collate("the black cat", "the black cat"));
    assertEquals("A: |the|black|cat|\nB: |the|black|cat|\n", toString(table));
  }

  @Test
  public void stringOutputEmptyCells() {
    assertEquals("A: |the|black|cat|\nB: |the| | |\n", toString(table(collate("the black cat", "the"))));
  }
}
