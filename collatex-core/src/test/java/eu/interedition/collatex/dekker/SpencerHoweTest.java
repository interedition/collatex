/*
 * Copyright (c) 2015 The Interedition Development Group.
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

package eu.interedition.collatex.dekker;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.util.VariantGraphRanking;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;

/**
 * Testing the dependence of the algorithm on the order of witnesses.
 *
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
        final SimpleWitness[] w = createWitnesses("a b c d e f", "x y z d e", "a b x y z");
        final List<SortedMap<Witness, Set<Token>>> table = VariantGraphRanking.of(collate(w)).asTable();

        assertEquals(3, table.stream().flatMap(r -> r.keySet().stream()).distinct().count());
        //NOTE: Currently the AT visualization aligns variation to the left of the table: see the 'C' element
        assertEquals("|a|b|c| | |d|e|f|", toString(table, w[0]));
        assertEquals("| | |x|y|z|d|e| |", toString(table, w[1]));
        assertEquals("|a|b|x|y|z| | | |", toString(table, w[2]));
    }

    @Test
    public void graph() {
        final SimpleWitness[] w = createWitnesses("a", "b", "a b");
        final VariantGraph graph = collate(w);

        assetGraphSize(graph, 4, 5);

        final VariantGraph.Vertex startVertex = graph.getStart();
        final VariantGraph.Vertex aVertex = vertexWith(graph, "a", w[0]);
        final VariantGraph.Vertex bVertex = vertexWith(graph, "b", w[1]);
        final VariantGraph.Vertex endVertex = graph.getEnd();

        assertHasWitnesses(edgeBetween(startVertex, aVertex), w[0], w[2]);
        assertHasWitnesses(edgeBetween(aVertex, endVertex), w[0]);
        assertHasWitnesses(edgeBetween(startVertex, bVertex), w[1]);
        assertHasWitnesses(edgeBetween(bVertex, endVertex), w[1], w[2]);
        assertHasWitnesses(edgeBetween(aVertex, bVertex), w[2]);
    }

}
