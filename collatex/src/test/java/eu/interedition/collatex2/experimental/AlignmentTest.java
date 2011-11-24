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

import eu.interedition.collatex2.AbstractTest;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlignmentTest extends AbstractTest {
  @Test
  public void transposition() {
    final IWitness[] w = createWitnesses("the cat is black", "black is the cat");
    final IAlignmentTable t = align(w);
    assertEquals("A: |the|cat|is|black| |", t.getRow(w[0]).toString());
    assertEquals("B: |black| |is|the|cat|", t.getRow(w[1]).toString());
  }

  @Test
  public void doubleTransposition2() {
    final IWitness[] w = createWitnesses("a b", "b a");
    final IAlignmentTable t = align(w);
    assertEquals("A: | |a|b|", t.getRow(w[0]).toString());
    assertEquals("B: |b|a| |", t.getRow(w[1]).toString());
  }

  @Test
  public void doubleTransposition3() {
    final IWitness[] w = createWitnesses("a b c", "b a c");
    final IAlignmentTable t = align(w);
    assertEquals("A: | |a|b|c|", t.getRow(w[0]).toString());
    assertEquals("B: |b|a| |c|", t.getRow(w[1]).toString());
  }

  @Test
  public void additionInCombinationWithTransposition() {
    final IWitness[] w = createWitnesses(//
            "the cat is very happy",//
            "very happy is the cat",//
            "very delitied and happy is the cat");
    final IAlignmentTable t = align(w);
    assertEquals("A: |the|cat| | |is|very|happy|", t.getRow(w[0]).toString());
    assertEquals("B: |very| | |happy|is|the|cat|", t.getRow(w[1]).toString());
    assertEquals("C: |very|delitied|and|happy|is|the|cat|", t.getRow(w[2]).toString());
  }

  @Test
  public void additionInCombinationWithTransposition2() {
    final IWitness[] w = createWitnesses(//
            "the cat is black",//
            "black is the cat",//
            "black and white is the cat");
    final IAlignmentTable t = align(w);
    assertEquals("A: |the|cat| |is|black| |", t.getRow(w[0]).toString());
    assertEquals("B: |black| | |is|the|cat|", t.getRow(w[1]).toString());
    assertEquals("C: |black|and|white|is|the|cat|", t.getRow(w[2]).toString());
  }

  @Test
  public void simpleTransposition() {
    final IWitness[] w = createWitnesses(//
            "A black cat in a white basket",//
            "A white cat in a black basket");
    final IAlignmentTable t = align(w);
    assertEquals("A: |A|black|cat|in|a|white|basket|", t.getRow(w[0]).toString());
    assertEquals("B: |A|white|cat|in|a|black|basket|", t.getRow(w[1]).toString());
  }

  @Test
  public void transposeInOnePair() {
    final IWitness[] w = createWitnesses("y", "x y z", "z y");
    final IAlignmentTable t = align(w);
    assertEquals("A: | |y| |", t.getRow(w[0]).toString());
    assertEquals("B: |x|y|z|", t.getRow(w[1]).toString());
    assertEquals("C: |z|y| |", t.getRow(w[2]).toString());
  }

  @Test
  public void transposeInTwoPairs() {
    final IWitness[] w = createWitnesses("y x", "x y z", "z y");
    final IAlignmentTable t = align(w);
    assertEquals("A: | |y|x|", t.getRow(w[0]).toString());
    assertEquals("B: |x|y|z|", t.getRow(w[1]).toString());
    assertEquals("C: |z|y| |", t.getRow(w[2]).toString());
  }
}
