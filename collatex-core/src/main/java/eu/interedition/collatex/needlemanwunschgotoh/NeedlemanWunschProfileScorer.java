/*
 * Copyright (c) 2016 The Interedition Development Group.
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

package eu.interedition.collatex.needlemanwunschgotoh;

import java.util.Set;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.matching.StringMetricScorer;

/**
 * Score against a profile.
 *
 * "Definition: Given a multiple alignment of a set of strings, a
 * <em>profile</em> for that multiple alignment specifies for each column the
 * <em>frequency</em> that each character appears in the column."  -- Gusfield
 * 1977, Algorithms on Strings, Trees and Sequences, Cambridge University Press
 *
 * The score for the full match is a weighted sum of scores based on the
 * frequency of the matched vertices.
 *
 * @author Marcello Perathoner
 */

public class NeedlemanWunschProfileScorer
    implements NeedlemanWunschScorer<Set<VariantGraph.Vertex>, Set<VariantGraph.Vertex>> {
    private final StringMetricScorer matchScorer;
    private final int size;

    public NeedlemanWunschProfileScorer(final StringMetricScorer matchScorer, final int size) {
        this.matchScorer = matchScorer;
        this.size = size;
    }

    @Override
    public double score(final Set<VariantGraph.Vertex> verticesA,
                        final Set<VariantGraph.Vertex> verticesB) {

        if (verticesA == null || verticesB == null) {
            return matchScorer.getMinScore();
        }
        if (verticesA.size() == 0 || verticesB.size() == 0) {
            return matchScorer.getMinScore();
        }

        double totalScore = 0.0;
        int totalMatched = 0;

        for (VariantGraph.Vertex vertexA : verticesA) {
            for (VariantGraph.Vertex vertexB : verticesB) {
                for (Token tokenA : vertexA.tokens()) {
                    final String a = ((SimpleToken) tokenA).getNormalized();
                    for (Token tokenB : vertexB.tokens()) {
                        final String b = ((SimpleToken) tokenB).getNormalized();
                        totalScore += matchScorer.score(a, b);
                        totalMatched++;
                    }
                }
            }
        }
        int totalUnmatched = size - totalMatched;

        return (totalScore + totalUnmatched * -1.0) / size;
    }
};
