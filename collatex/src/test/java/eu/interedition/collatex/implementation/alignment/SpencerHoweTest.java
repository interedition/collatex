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

package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.RowSortedTable;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.graph.db.VariantGraph;
import eu.interedition.collatex.implementation.graph.db.VariantGraphVertex;
import eu.interedition.collatex.interfaces.*;
import org.junit.Test;

import java.util.SortedSet;

import static org.junit.Assert.assertEquals;

/**
 * Testing the dependence of the algorithm on the order of witnesses.
 * <p/>
 * <p>
 * See Matthew Spencer and Christopher J. Howe
 * "Collating Texts Using Progressive Multiple Alignment".
 * </p>
 *
 * @author Gregor Middell
 */
public class SpencerHoweTest extends AbstractTest {

  @Test
  public void alignmentTable() {
    final IWitness[] w = createWitnesses("a b c d e f", "x y z d e", "a b x y z");
    final RowSortedTable<Integer, IWitness, SortedSet<Token>> table = merge(w).toTable();

    assertEquals(3, table.columnKeySet().size());
    //NOTE: Currently the AT visualization aligns variation to the left of the table: see the 'C' element
    assertEquals("|a|b|c| | |d|e|f|", toString(table, w[0]));
    assertEquals("| | |x|y|z|d|e| |", toString(table, w[1]));
    assertEquals("|a|b|x|y|z| | | |", toString(table, w[2]));
  }

  @Test
  public void graph() {
    final IWitness[] w = createWitnesses("a", "b", "a b");
    final VariantGraph graph = merge(w);
    assertEquals(4, Iterables.size(graph.vertices()));
    assertEquals(5, Iterables.size(graph.edges()));

    final VariantGraphVertex startVertex = graph.getStart();
    final VariantGraphVertex aVertex = vertexWith(graph, "a", w[0]);
    final VariantGraphVertex bVertex = vertexWith(graph, "b", w[1]);
    final VariantGraphVertex endVertex = graph.getEnd();

    assertHasWitnesses(edgeBetween(startVertex, aVertex), w[0], w[2]);
    assertHasWitnesses(edgeBetween(aVertex, endVertex), w[0]);
    assertHasWitnesses(edgeBetween(startVertex, bVertex), w[1]);
    assertHasWitnesses(edgeBetween(bVertex, endVertex), w[1], w[2]);
    assertHasWitnesses(edgeBetween(aVertex, bVertex), w[2]);
  }

}
