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

package eu.interedition.collatex2.alignmenttable;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class TranspositionAlignmentTest {
  @Ignore
  @Test
  public void transposeInOnePair() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("A", "y");
    IWitness b = engine.createWitness("B", "x y z");
    IWitness c = engine.createWitness("C", "z y");
    Assert.assertEquals("A:  | |y| \n" + "B: x| |y|z\n" + "C:  |z|y| \n", engine.align(a, b, c).toString());
  }

  //TODO: for this test to work we need VariantGraph based alignment!
  @Test
  @Ignore
  public void transposeInTwoPairs() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("A", "y x");
    IWitness b = engine.createWitness("B", "x y z");
    IWitness c = engine.createWitness("C", "z y");
    Assert.assertEquals("A:  |y|x| \nB: x|y|z\nC: z|y| \n", engine.align(a, b, c).toString());
  }
}
