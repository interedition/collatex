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
import java.util.Set;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

public class MatchesTest extends AbstractTest {

    @Test
    public void test1() {
        final SimpleWitness[] w = createWitnesses("john and paul and george and ringo", "john and paul and george and ringo");
        final VariantGraph graph = collate(w[0]);
        final Matches matches = Matches.between(graph.vertices(), w[1].getTokens(), new EqualityTokenComparator());

        int expected_unmatched = 0;
        int expected_unique = 4; // john paul george ringo
        int expected_ambiguous = 3; // 3 ands in 2nd witness
        assertMatches(matches, expected_unmatched, expected_unique, expected_ambiguous);
    }

    @Test
    public void test2() {
        final SimpleWitness[] w = createWitnesses("the white cat", "the black cat");
        final VariantGraph graph = collate(w[0]);
        final Matches matches = Matches.between(graph.vertices(), w[1].getTokens(), new EqualityTokenComparator());

        int expected_unmatched = 1; // black
        int expected_unique = 2; // the & cat
        int expected_ambiguous = 0;
        assertMatches(matches, expected_unmatched, expected_unique, expected_ambiguous);
    }

    // This test tests overlapping islands
    @Test
    public void test3OverlappingIslands() {
        String witnessA = "the cat and the dog";
        String witnessB = "the dog and the cat";
        SimpleWitness[] sw = createWitnesses(witnessA, witnessB);
        VariantGraph vg = collate(sw[0]);
        final Matches matches = Matches.between(vg.vertices(), sw[1].getTokens(), new EqualityTokenComparator());
        assertMatches(matches, 0, 3, 2);
        assertEquals(7, matches.allMatches.values().stream().mapToLong(List::size).sum());
    }


    private void assertMatches(final Matches matches, int expected_unmatched, int expected_unique, int expected_ambiguous) {
        Set<Token> unmatched = matches.unmatchedInWitness;
        LOG.log(Level.FINE, "unmatched: {0}", unmatched);

        Set<Token> unique = matches.uniqueInWitness;
        LOG.log(Level.FINE, "unique: {0}", unique);

        Set<Token> ambiguous = matches.ambiguousInWitness;
        LOG.log(Level.FINE, "ambiguous: {0}", ambiguous);

        Map<Token, List<VariantGraph.Vertex>> all = matches.allMatches;
        LOG.log(Level.FINE, "all: {0}", all);

        assertEquals(expected_unmatched, unmatched.size());
        assertEquals(expected_unique, unique.size());
        assertEquals(expected_ambiguous, ambiguous.size());
        //    assertEquals(expected_unique + expected_ambiguous, all.size());
    }
}
