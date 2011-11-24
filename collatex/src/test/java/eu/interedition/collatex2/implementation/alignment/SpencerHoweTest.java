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

package eu.interedition.collatex2.implementation.alignment;

import static org.junit.Assert.assertEquals;

import eu.interedition.collatex2.AbstractTest;
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
public class SpencerHoweTest extends AbstractTest {

  @Test
  public void spencerHowe() {
    final IWitness[] w = createWitnesses("a b c d e f ", "x y z d e", "a b x y z");
    final IAlignmentTable table = toAlignmentTable(w);

    assertEquals(3, table.getRows().size());
    //NOTE: Currently the AT visualization aligns variation to the left of the table: see the 'C' element
    assertEquals("A: |a|b|c| | |d|e|f|", table.getRow(w[0]).toString());
    assertEquals("B: | | |x|y|z|d|e| |", table.getRow(w[1]).toString());
    assertEquals("C: |a|b|x|y|z| | | |", table.getRow(w[2]).toString());
  }
}
