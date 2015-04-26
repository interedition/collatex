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

package eu.interedition.collatex.dekker.matrix;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IslandTest extends AbstractTest {

    String newLine = System.getProperty("line.separator");

    @Test
    public void testCoordinates() {
        Coordinate a = new Coordinate(0, 0);
        Coordinate b = new Coordinate(0, 0);
        Coordinate c = new Coordinate(1, 1);
        assertEquals(new Coordinate(0, 0), a);
        assertEquals(b, a);
        assertFalse(a.equals(c));
    }

    @Test
    public void testBorders() {
        Coordinate a = new Coordinate(0, 0);
        Coordinate b = new Coordinate(1, 1);
        Coordinate c = new Coordinate(1, 2);
        assertTrue(a.bordersOn(b));
        assertFalse(a.bordersOn(c));
        assertFalse(b.bordersOn(c));
    }

    @Test
    public void testUndirectedIsland() {
        Island isl = new Island();
        isl.add(new Coordinate(0, 0));
        assertEquals(1, isl.size());
        isl.add(new Coordinate(0, 0));
        assertEquals(1, isl.size());
        isl.add(new Coordinate(1, 0));
        assertEquals(1, isl.size());
        isl.add(new Coordinate(2, 2));
        assertEquals(1, isl.size());
        assertTrue(isl.neighbour(new Coordinate(1, 1)));
        isl.add(new Coordinate(1, 1));
        assertEquals(2, isl.size());
    }

    @Test
    public void testDirectedIsland() {
        Island isl = new Island();
        isl.add(new Coordinate(0, 0));
        assertEquals(1, isl.size());
        assertEquals(0, isl.direction());
        isl.add(new Coordinate(1, 1));
        assertEquals(2, isl.size());
        assertEquals(1, isl.direction());
        isl.add(new Coordinate(2, 2));
        assertEquals(3, isl.size());
        assertEquals(1, isl.direction());
    }

    @Test
    public void testArchipelago() {
        Archipelago arch = new Archipelago();
        Island isl_1 = new Island();
        isl_1.add(new Coordinate(0, 0));
        isl_1.add(new Coordinate(1, 1));
        arch.add(isl_1);
        Island isl_2 = new Island();
        isl_2.add(new Coordinate(2, 2));
        isl_2.add(new Coordinate(3, 3));
        arch.add(isl_2);
        assertEquals(2, arch.size());
        assertTrue(isl_1.overlap(isl_2));
    }

    @Test
    public void testArchipelagoRivalIslands() {
        SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
        VariantGraph vg = collate(sw[0]);
        MatchTable table = MatchTableImpl.create(vg, sw[1], new EqualityTokenComparator());
        Archipelago archipelago = new Archipelago();
        for (Island isl : table.getIslands()) {
            archipelago.add(isl);
        }
        assertEquals(3, archipelago.size());
    }

    @Test
    public void testRemovePoints() {
        Island di_1 = new Island();
        di_1.add(new Coordinate(1, 1));
        di_1.add(new Coordinate(2, 2));
        Island di_2 = new Island();
        di_2.add(new Coordinate(2, 2));
        Island di_3 = di_1.removePoints(di_2);
        assertEquals(2, di_1.size());
        assertEquals(1, di_3.size());
    }

    @Test
    public void testFindCoorOnRowOrCol() {
        Island isl_1 = new Island();
        isl_1.add(new Coordinate(0, 0));
        isl_1.add(new Coordinate(1, 1));
        assertEquals(new Coordinate(0, 0), isl_1.getCoorOnRow(0));
        assertEquals(new Coordinate(1, 1), isl_1.getCoorOnCol(1));
        assertEquals(null, isl_1.getCoorOnCol(4));
    }

    @Test
    public void testIslandValue() {
        Island isl_1 = new Island();
        isl_1.add(new Coordinate(1, 1));
        assertEquals(1, isl_1.value());
        isl_1.add(new Coordinate(2, 2));
        assertEquals(5, isl_1.value());
        isl_1.add(new Coordinate(3, 3));
        assertEquals(10, isl_1.value());
        Island isl_2 = new Island();
        isl_2.add(new Coordinate(2, 2));
        isl_2.add(new Coordinate(1, 3));
        assertEquals(3, isl_2.value());
    }
}
