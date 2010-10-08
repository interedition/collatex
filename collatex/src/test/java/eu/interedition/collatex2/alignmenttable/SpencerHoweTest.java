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

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.experimental.output.table.VariantGraphBasedAlignmentTable;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2Creator;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IVariantGraph;
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
  private static Logger logger = LoggerFactory.getLogger(SpencerHoweTest.class);

  private CollateXEngine engine = new CollateXEngine();

  //TODO: need variant graph based alignment for this test to work!
  @Test
  @Ignore
  public void testATSpencerHowe() {
    final IWitness w1 = engine.createWitness("V", "a b c d e f ");
    final IWitness w2 = engine.createWitness("W", "x y z d e");
    final IWitness w3 = engine.createWitness("X", "a b x y z");
    IVariantGraph graph = VariantGraph2Creator.create(w1, w2, w3);
    IAlignmentTable table = new VariantGraphBasedAlignmentTable(graph);
    assertEquals("V: |a|b|c| | |d|e|f|", table.getRow(w1).rowToString());
    assertEquals("W: | | |x|y|z|d|e| |", table.getRow(w2).rowToString());
    assertEquals("X: |a|b|x|y|z| | | |", table.getRow(w3).rowToString());
    assertEquals(3, table.getRows().size());
  }
}
