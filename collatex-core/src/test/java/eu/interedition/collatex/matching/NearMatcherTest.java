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

package eu.interedition.collatex.matching;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NearMatcherTest extends AbstractTest {

    @Test
    public void nearTokenMatching() {
        final SimpleWitness[] w = createWitnesses("near matching yeah", "nar matching");
        final VariantGraph graph = collate(w[0]);
        final Map<Token, List<VariantGraph.Vertex>> matches = Matches.between(graph.vertices(), w[1].getTokens(), new EditDistanceTokenComparator()).allMatches;

        assertEquals(2, matches.values().stream().mapToLong(List::size).sum());
        assertEquals(w[0].getTokens().get(0), matches.get(w[1].getTokens().get(0)).get(0).tokens().stream().findFirst().get());
        assertEquals(w[0].getTokens().get(1), matches.get(w[1].getTokens().get(1)).get(0).tokens().stream().findFirst().get());
    }
}
