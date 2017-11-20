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

package eu.interedition.collatex.dekker.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.island.Coordinate;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MatchTableTest extends AbstractTest {

    // helper method
    private void assertIslandEquals(int leftRow, int leftColumn, int rightRow, int rightColumn, Island island) {
        Coordinate leftEnd = island.getLeftEnd();
        assertEquals(leftRow, leftEnd.getRow());
        assertEquals(leftColumn, leftEnd.getColumn());
        Coordinate rightEnd = island.getRightEnd();
        assertEquals(rightRow, rightEnd.getRow());
        assertEquals(rightColumn, rightEnd.getColumn());
    }

    // helper method
    // note: x = x of start coordinate
    // note: y = y of start coordinate
    //TODO: replace Island by a real Vector class
    private void assertVectorEquals(int x, int y, int length, Island island) {
        Coordinate leftEnd = island.getLeftEnd();
        assertEquals(x, leftEnd.getRow());
        assertEquals(y, leftEnd.getColumn());
        assertEquals(length, island.size());
    }

    @Test
    public void testTableCreationEmptyGraph() {
        final VariantGraph graph = new VariantGraph();
        SimpleWitness[] witnesses = createWitnesses("a b");
        MatchTable table = MatchTableImpl.create(graph, witnesses[0]);
        assertEquals(0, table.columnList().size());
    }

    @Test
    public void testTableCreationVariationDoesNotCauseExtraColumns() {
        SimpleWitness[] witnesses = createWitnesses("a", "b", "c", "d");
        VariantGraph graph = collate(witnesses[0], witnesses[1], witnesses[2]);
        MatchTable table = MatchTableImpl.create(graph, witnesses[3]);
        assertEquals(1, table.columnList().size());
    }

    @Test
    public void testTableCreationAbAcAbc() {
        SimpleWitness[] witnesses = createWitnesses("a b", "a c", "a b c");
        VariantGraph graph = collate(witnesses[0], witnesses[1]);
        MatchTable table = MatchTableImpl.create(graph, witnesses[2]);
        assertVertexEquals("a", table.vertexAt(0, 0));
        assertVertexEquals("b", table.vertexAt(1, 1));
        assertVertexEquals("c", table.vertexAt(2, 1));
    }

    @Test
    public void testTableCreationAbcabCab() {
        SimpleWitness[] witnesses = createWitnesses("a b c a b", "c a b");
        VariantGraph graph = collate(witnesses[0]);
        MatchTable table = MatchTableImpl.create(graph, witnesses[1]);
        assertVertexEquals("a", table.vertexAt(1, 0));
        assertVertexEquals("b", table.vertexAt(2, 1));
        assertVertexEquals("c", table.vertexAt(0, 2));
        assertVertexEquals("a", table.vertexAt(1, 3));
        assertVertexEquals("b", table.vertexAt(2, 4));
    }

    @Test
    public void testTableCreationAbcabAbcab() {
        SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
        VariantGraph vg = collate(sw[0]);
        MatchTable table = MatchTableImpl.create(vg, sw[1], new EqualityTokenComparator());
        assertEquals(5, table.columnList().size());
        assertEquals(5, table.rowList().size());
        assertVertexEquals("a", table.vertexAt(0, 0));
        assertVertexEquals("a", table.vertexAt(0, 3));
        assertVertexEquals("b", table.vertexAt(1, 1));
        assertVertexEquals("b", table.vertexAt(1, 4));
        assertVertexEquals("c", table.vertexAt(2, 2));
        assertVertexEquals("a", table.vertexAt(3, 0));
        assertVertexEquals("a", table.vertexAt(3, 3));
        assertVertexEquals("b", table.vertexAt(4, 1));
        assertVertexEquals("b", table.vertexAt(4, 4));
    }

    @Test
    public void testTableCreationAsymmatricMatrix() {
        SimpleWitness[] sw = createWitnesses("A B A B C", "A B C A B");
        VariantGraph vg = collate(sw[0]);
        MatchTable table = MatchTableImpl.create(vg, sw[1], new EqualityTokenComparator());
        assertVertexEquals("a", table.vertexAt(0, 0));
        assertVertexEquals("a", table.vertexAt(0, 2));
        assertVertexEquals("b", table.vertexAt(1, 1));
        assertVertexEquals("b", table.vertexAt(1, 3));
        assertVertexEquals("c", table.vertexAt(2, 4));
        assertVertexEquals("a", table.vertexAt(3, 0));
        assertVertexEquals("a", table.vertexAt(3, 2));
        assertVertexEquals("b", table.vertexAt(4, 1));
        assertVertexEquals("b", table.vertexAt(4, 3));
    }

    @Test
    public void testRowLabels() {
        String textD1 = "de het een";
        String textD9 = "het een de";
        SimpleWitness[] sw = createWitnesses(textD1, textD9);
        VariantGraph vg = collate(sw[0]);
        MatchTable table = MatchTableImpl.create(vg, sw[1], new EqualityTokenComparator());
        List<Token> labels = table.rowList();
        assertTokenEquals("het ", labels.get(0));
        assertTokenEquals("een ", labels.get(1));
        assertTokenEquals("de", labels.get(2));
    }

    @Test
    public void testColumnLabels() {
        String textD1 = "de het een";
        String textD9 = "het een de";
        SimpleWitness[] sw = createWitnesses(textD1, textD9);
        VariantGraph vg = collate(sw[0]);
        MatchTable table = MatchTableImpl.create(vg, sw[1], new EqualityTokenComparator());
        List<Integer> labels = table.columnList();
        assertEquals((Integer) 0, labels.get(0));
        assertEquals((Integer) 1, labels.get(1));
        assertEquals((Integer) 2, labels.get(2));
    }

    @Test
    public void testGetAllMatches() {
        SimpleWitness[] sw = createWitnesses("A B A B C", "A B C A B");
        VariantGraph vg = collate(sw[0]);
        MatchTableImpl table = MatchTableImpl.create(vg, sw[1], new EqualityTokenComparator());
        List<Coordinate> allTrue = table.allMatches();
        assertEquals(9, allTrue.size());
        assertTrue(allTrue.contains(new Coordinate(0, 0)));
        assertFalse(allTrue.contains(new Coordinate(0, 1)));
    }

    @Test
    public void testIslandDetectionAbcabCab() {
        SimpleWitness[] witnesses = createWitnesses("a b c a b", "c a b");
        VariantGraph graph = collate(witnesses[0]);
        MatchTable table = MatchTableImpl.create(graph, witnesses[1]);
        List<Island> islands = new ArrayList<>(table.getIslands());
        assertEquals(2, islands.size());
        islands.sort(new IslandPositionComparator());
        Island island = islands.get(1);
        assertIslandEquals(0, 2, 2, 4, island);
    }

    @Test
    public void testIslandDetectionXabcabXcab() {
        SimpleWitness[] witnesses = createWitnesses("x a b c a b", "x c a b");
        VariantGraph graph = collate(witnesses[0]);
        MatchTable table = MatchTableImpl.create(graph, witnesses[1]);
        List<Island> islands = new ArrayList<>(table.getIslands());
        assertEquals(3, islands.size());
        islands.sort(new IslandPositionComparator());
        Island island = islands.get(0);
        assertIslandEquals(0, 0, 0, 0, island);
    }

    @Test
    public void testIslandDetectionPartlyOverlappingIslandsUsecase() {
        SimpleWitness[] w = createWitnesses("The cat and the dog", "the dog and the cat");
        VariantGraph graph = collate(w[0]);
        MatchTable table = MatchTableImpl.create(graph, w[1], new EqualityTokenComparator());
        List<Island> islands = new ArrayList<>(table.getIslands());
        islands.sort(new IslandPositionComparator());
        assertEquals(4, islands.size());
        assertVectorEquals(0, 0, 1, islands.get(0));
        assertVectorEquals(3, 0, 2, islands.get(1));
        assertVectorEquals(2, 2, 2, islands.get(2));
        assertVectorEquals(0, 3, 2, islands.get(3));
    }
}
