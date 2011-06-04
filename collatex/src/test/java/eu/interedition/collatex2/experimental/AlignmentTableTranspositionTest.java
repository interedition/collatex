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

package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableTranspositionTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new MyNewCollateXEngine();
  }

  @Test
  public void testTransposition() {
    IWitness a = engine.createWitness("A", "the cat is black");
    IWitness b = engine.createWitness("B", "black is the cat");
    IAlignmentTable table = engine.align(a, b);
    assertEquals("A: |the|cat|is|black| |", table.getRow(a).toString());
    assertEquals("B: |black| |is|the|cat|", table.getRow(b).toString());
  }

  @Test
  public void testDoubleTransposition2() {
    IWitness a = engine.createWitness("A", "a b");
    IWitness b = engine.createWitness("B", "b a");
    IAlignmentTable alignmentTable = engine.align(a, b);
    assertEquals("A: | |a|b|", alignmentTable.getRow(a).toString());
    assertEquals("B: |b|a| |", alignmentTable.getRow(b).toString());
  }

  @Test
  public void testDoubleTransposition3() {
    IWitness a = engine.createWitness("A", "a b c");
    IWitness b = engine.createWitness("B", "b a c");
    IAlignmentTable alignmentTable = engine.align(a, b);
    assertEquals("A: | |a|b|c|", alignmentTable.getRow(a).toString()); 
    assertEquals("B: |b|a| |c|", alignmentTable.getRow(b).toString());
  }

  @Test
  public void testAdditionInCombinationWithTransposition() {
    IWitness a = engine.createWitness("A", "the cat is very happy");
    IWitness b = engine.createWitness("B", "very happy is the cat");
    IWitness c = engine.createWitness("C", "very delitied and happy is the cat");
    IAlignmentTable table = engine.align(a, b, c);
    assertEquals("A: |the|cat| | |is|very|happy|", table.getRow(a).toString());
    assertEquals("B: |very| | |happy|is|the|cat|", table.getRow(b).toString()); 
    assertEquals("C: |very|delitied|and|happy|is|the|cat|", table.getRow(c).toString());
  }

  @Test
  public void testAdditionInCombinationWithTransposition2() {
    final IWitness a = engine.createWitness("A", "the cat is black");
    final IWitness b = engine.createWitness("B", "black is the cat");
    final IWitness c = engine.createWitness("C", "black and white is the cat");
    final IAlignmentTable table = engine.align(a, b, c);
    assertEquals("A: |the|cat| |is|black| |", table.getRow(a).toString());
    assertEquals("B: |black| | |is|the|cat|", table.getRow(b).toString());
    assertEquals("C: |black|and|white|is|the|cat|", table.getRow(c).toString());
  }
  
  // Test made by Gregor Middell
  @Test
  public void testSimpleTransposition() {
    final IWitness w1 = engine.createWitness("A", "A black cat in a white basket");
    final IWitness w2 = engine.createWitness("B", "A white cat in a black basket");
    final IAlignmentTable table = engine.align(w1, w2);
    assertEquals("A: |A|black|cat|in|a|white|basket|", table.getRow(w1).toString());
    assertEquals("B: |A|white|cat|in|a|black|basket|", table.getRow(w2).toString());
  }

////Note: this is more of an alignment test.. no table is involved here! 
//@Test
//public void testNoTransposition() {
//  final IWitness a = engine.createWitness("A", "no transposition");
//  final IWitness b = engine.createWitness("B", "no transposition");
//  final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
//  Assert.assertTrue(al.getTranspositions().isEmpty());
//}
//
////Note: this is more of an alignment test.. no table is involved here! 
//@Test
//public void testNoTransposition2() {
//  final IWitness a = engine.createWitness("A", "a b");
//  final IWitness b = engine.createWitness("B", "c a");
//  final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
//  Assert.assertTrue(al.getTranspositions().isEmpty());
//}

////Note: this is more of an alignment test.. no table is involved here! 
//@Test
//public void testDoubleTransposition() {
//  final IWitness a = engine.createWitness("A", "a b");
//  final IWitness b = engine.createWitness("B", "b a");
//  final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
//  Assert.assertEquals(2, al.getTranspositions().size());
//  // 1: a -> b
//  // 2: b -> a
//}
//
////Note: this is more of an alignment test.. no table is involved here! 
//@Test
//public void testMultipleTransposition() {
//  final IWitness a = engine.createWitness("A", "a b c");
//  final IWitness b = engine.createWitness("B", "b c a");
//  final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
//  Assert.assertEquals(2, al.getTranspositions().size());
//  // 1: a -> b c
//  // 2: b c -> a
//}

}
