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
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class IslandConflictResolverTest extends AbstractTest {

    // 3 islands of 2, 1 island of size 1
    // the 3 islands of size 2 overlap partly
    //TODO: add new IslandCompetitionType: party overlapping!
    @Test
    public void testPartlyOverlappingIslands() {
        // create two witnesses
        SimpleWitness[] w = createWitnesses("The cat and the dog", "the dog and the cat");
        // create graph from the first witness
        VariantGraph graph = collate(w[0]);
        // create table from the graph and the second witness
        MatchTable table = MatchTableImpl.create(graph, w[1]);
        List<Island> possibleIslands = new ArrayList<>();
        for (Island island : table.getIslands()) {
            if (island.size() == 2) {
                possibleIslands.add(island);
            }
        }
        IslandConflictResolver resolver = new IslandConflictResolver(table.getIslands());
        Map<IslandCompetition, List<Island>> competition = resolver.analyzeConflictsBetweenPossibleIslands(possibleIslands);
        assertEquals(3, competition.get(IslandCompetition.CompetingIsland).size());
    }
}
