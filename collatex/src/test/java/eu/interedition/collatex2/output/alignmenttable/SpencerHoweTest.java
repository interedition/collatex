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

package eu.interedition.collatex2.output.alignmenttable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

/**
 * Testing the dependence of the algorithm on the order of witnesses.
 * 
 * <p>
 * See Matthew Spencer and Christopher J. Howe
 * "Collating Texts Using Progressive Multiple Alignment".
 * </p>
 * 
 * @author Gregor Middell
 * 
 */
public class SpencerHoweTest {
  private CollateXEngine engine = new CollateXEngine();

  //NOTE: Currently the AT visualization aligns variation to the
  //left of the table: see the 'C' element
  @Test
  public void testATSpencerHowe() {
    final IWitness w1 = engine.createWitness("V", "a b c d e f ");
    final IWitness w2 = engine.createWitness("W", "x y z d e");
    final IWitness w3 = engine.createWitness("X", "a b x y z");
    IAlignmentTable table = engine.align(w1, w2, w3);
    assertEquals("V: |a|b|c| | |d|e|f|", table.getRow(w1).rowToString());
    assertEquals("W: | | |x|y|z|d|e| |", table.getRow(w2).rowToString());
    assertEquals("X: |a|b|x|y|z| | | |", table.getRow(w3).rowToString());
    assertEquals(3, table.getRows().size());
  }
}
