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

package eu.interedition.collatex.needlemanwunschgotoh;

import java.util.Set;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.matching.StringMetricScorer;

/**
 * Score a set of vertices against another set of vertices
 *
 * The score will be the highest score achieved while matching all tokens of all
 * vertices in verticesA against all tokens of all vertices in verticesB.
 *
 * @author Marcello Perathoner
 */

public class NeedlemanWunschScorerSetVertexSetVertex
    implements NeedlemanWunschScorer<Set<VariantGraph.Vertex>, Set<VariantGraph.Vertex>> {
    private final StringMetricScorer matchScorer;

    public NeedlemanWunschScorerSetVertexSetVertex(final StringMetricScorer matchScorer) {
        this.matchScorer = matchScorer;
    }

    public class Match {
        public final VariantGraph.Vertex vertexA;
        public final VariantGraph.Vertex vertexB;
        public final double score;
        public Match(final VariantGraph.Vertex vertexA,
                     final VariantGraph.Vertex vertexB,
                     final double score) {
            this.vertexA = vertexA;
            this.vertexB = vertexB;
            this.score = score;
        }
    }

    public Match match(final Set<VariantGraph.Vertex> verticesA,
                       final Set<VariantGraph.Vertex> verticesB,
                       final double minScore) {

        if (verticesA == null || verticesB == null) {
            return null;
        }
        if (verticesA.size() == 0 || verticesB.size() == 0) {
            return null;
        }

        Match matching = null;
        double minScoreMatched = minScore;

        for (VariantGraph.Vertex vertexA : verticesA) {
            for (VariantGraph.Vertex vertexB : verticesB) {

                for (Token tokenA : vertexA.tokens()) {
                    final String a = ((SimpleToken) tokenA).getNormalized();

                    for (Token tokenB : vertexB.tokens()) {
                        final String b = ((SimpleToken) tokenB).getNormalized();

                        double score = matchScorer.score(a, b);
                        if (score > minScoreMatched) {
                            minScoreMatched = score;
                            matching = new Match(vertexA, vertexB, score);
                        }
                    }
                }
            }
        }
        return matching;
    }

    @Override
    public double score(final Set<VariantGraph.Vertex> verticesA,
                        final Set<VariantGraph.Vertex> verticesB) {
        Match matching = match(verticesA, verticesB, matchScorer.getMinScore());
        return (matching != null) ? matching.score : matchScorer.getMinScore();
    }
};
