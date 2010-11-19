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
import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableTranspositionTest {
  private static Logger logger = LoggerFactory.getLogger(AlignmentTableTranspositionTest.class);
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

//  //Note: this is more of an alignment test.. no table is involved here! 
//  @Test
//  public void testNoTransposition() {
//    final IWitness a = engine.createWitness("A", "no transposition");
//    final IWitness b = engine.createWitness("B", "no transposition");
//    final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
//    Assert.assertTrue(al.getTranspositions().isEmpty());
//  }
//
//  //Note: this is more of an alignment test.. no table is involved here! 
//  @Test
//  public void testNoTransposition2() {
//    final IWitness a = engine.createWitness("A", "a b");
//    final IWitness b = engine.createWitness("B", "c a");
//    final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
//    Assert.assertTrue(al.getTranspositions().isEmpty());
//  }

//  //Note: this is more of an alignment test.. no table is involved here! 
//  @Test
//  public void testDoubleTransposition() {
//    final IWitness a = engine.createWitness("A", "a b");
//    final IWitness b = engine.createWitness("B", "b a");
//    final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
//    Assert.assertEquals(2, al.getTranspositions().size());
//    // 1: a -> b
//    // 2: b -> a
//  }
//
//  //Note: this is more of an alignment test.. no table is involved here! 
//  @Test
//  public void testMultipleTransposition() {
//    final IWitness a = engine.createWitness("A", "a b c");
//    final IWitness b = engine.createWitness("B", "b c a");
//    final IAlignment al = PairwiseAlignmentHelper.align(engine, a, b);
//    Assert.assertEquals(2, al.getTranspositions().size());
//    // 1: a -> b c
//    // 2: b c -> a
//  }

  //TODO: rewrite test to work with the new API
  @Ignore
  @Test
  public void testDoubleTransposition2() {
    final IWitness a = engine.createWitness("A", "a b");
    final IWitness b = engine.createWitness("B", "b a");
    final IAlignmentTable alignmentTable = engine.align(a, b);
    final String expected = "A:  |a|b\n" + "B: b|a| \n";
    final String actual = alignmentTable.toString();
    Assert.assertEquals(expected, actual);
  }


  @Ignore
  @Test
  public void testDoubleTransposition3() {
    final IWitness a = engine.createWitness("A", "a b c");
    final IWitness b = engine.createWitness("B", "b a c");
    final IAlignmentTable alignmentTable = engine.align(a, b);
    final String expected = "A:  |a|b|c\n" + "B: b|a| |c\n";
    final String actual = alignmentTable.toString();
    Assert.assertEquals(expected, actual);
  }

  @Ignore
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

  @Ignore
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

  @Ignore
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
  @Ignore
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
