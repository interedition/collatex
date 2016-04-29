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

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.matching.Pair;
import eu.interedition.collatex.matching.StringMetricScorer;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Implements the Needleman-Wunsch-Gotoh collation algorithm.
 *
 * @author Marcello Perathoner
 */
public class NeedlemanWunschGotohAlgorithm implements CollationAlgorithm {

    private final StringMetricScorer scorer;
    /** The minimum similarity score to merge two tokens into one vertex. */
    private final double matchScore;
    /** A human-readable matrix. Written to if set. */
    private StringBuilder debugMatrix = null;

    public NeedlemanWunschGotohAlgorithm(final StringMetricScorer scorer) {
        this.scorer = scorer;
        this.matchScore = 0.6;
    }

    @Override
    public void collate(final VariantGraph graph, final Iterable<Token> witness) {
        List<Iterable<Token>> witnesses = new ArrayList<>();
        witnesses.add(witness);
        collate(graph, witnesses);
    };

    @Override
    public void collate(final VariantGraph graph, final Iterable<Token>... witnesses) {
        collate(graph, Arrays.asList(witnesses));
    };

    @Override
    public void collate(final VariantGraph graph, final List<? extends Iterable<Token>> witnesses) {
        List<Set<VariantGraph.Vertex>> rankingA = graphToRanking(graph);
        Set<Witness> witnessesA = getWitnesses(rankingA);

        for (Iterable<Token> witness : witnesses) {
            final List<Set<VariantGraph.Vertex>> rankingB = tokensToRanking(witness);
            final Set<Witness> witnessesB = getWitnesses(rankingB);

            rankingA = doCollate(rankingA, witnessesA, rankingB, witnessesB);
            witnessesA.addAll(witnessesB);
        }
        rankingToGraph(graph, rankingA);
    };

    public void setDebugMatrix(final StringBuilder debugMatrix) {
        this.debugMatrix = debugMatrix;
    }

    /* Incipit private stuff */

    private Set<Witness> getWitnesses(final VariantGraph.Vertex vertex) {
        return vertex.tokens().stream().map(t -> t.getWitness()).collect(Collectors.toSet());
    }

    private Set<Witness> getWitnesses(final Collection<Set<VariantGraph.Vertex>> ranks) {
        return ranks.stream()
            .flatMap(s -> s.stream())
            .flatMap(t -> t.tokens().stream())
            .map(t -> t.getWitness())
            .collect(Collectors.toSet());
    }

    private Set<VariantGraph.Vertex> tokenToVertexSet(final Token t) {
        Set<VariantGraph.Vertex> vertexSet = new HashSet<>();
        VariantGraph.Vertex vertex = new VariantGraph.Vertex(null);
        vertex.tokens().add(t);
        vertexSet.add(vertex);
        return vertexSet;
    }

    /**
     * Import a set of vertices into a new graph
     *
     * Makes a copy of the vertex and imports it into the new graph.  Does not
     * connect the vertices.
     *
     * @param graph     The graph into which to import the vertices
     * @param vertices  The vertices to import
     * @return          A set of copied and imported vertices
     */
    private Set<VariantGraph.Vertex> importVertexSet(
            final VariantGraph graph,
            final Set<VariantGraph.Vertex> vertices) {
        return vertices.stream()
            .map(vertex -> {
                    VariantGraph.Vertex v = new VariantGraph.Vertex(graph);
                    v.tokens().addAll(vertex.tokens());
                    return v;
                }
            ).collect(Collectors.toSet());
    }

    /**
     * Create a ranking from a graph.
     *
     * @param graph  The graph
     * @return       The ranking
     */
    private List<Set<VariantGraph.Vertex>> graphToRanking(final VariantGraph graph) {
        final List<Set<VariantGraph.Vertex>> ranking = new ArrayList<>();
        Iterator<Set<VariantGraph.Vertex>> iter = VariantGraphRanking.of(graph).iterator();
        while (iter.hasNext()) {
            ranking.add(importVertexSet(null, iter.next()));
        }
        ranking.remove(0);                  // shave off graph start element
        ranking.remove(ranking.size() - 1); // shave off graph end element
        return ranking;
    }

    /**
     * Create a list of {@code Set<VariantGraph.Vertex>} from a witness.
     *
     * The rationale behind this conversion is to make both inputs to the
     * collator be of the same type.  This because a symmetric problem is
     * generally easier to solve.
     *
     * @param iter  A witness as token stream
     * @return      A ranking with one token in each rank
     */
    private List<Set<VariantGraph.Vertex>> tokensToRanking(final Iterable<Token> iter) {
        final List<Set<VariantGraph.Vertex>> ranking = new ArrayList<>();
        for (Token t : iter) {
            ranking.add(tokenToVertexSet(t));
        }
        return ranking;
    }

    /**
     * Collate two rankings
     *
     * The aligner decides which ranks of rankingA and rankingB to align.  If
     * the alignment is good enough, two vertices, one in each ranking, are
     * merged.
     *
     * @param  rankingA    A ranking
     * @param  witnessesA  All witnesses in rankingA
     * @param  rankingB    A ranking
     * @param  witnessesB  All witnesses in rankingB
     * @return             The collated ranking
     */

    private List<Set<VariantGraph.Vertex>> doCollate(final Collection<Set<VariantGraph.Vertex>> rankingA,
                                                     final Set<Witness> witnessesA,
                                                     final Collection<Set<VariantGraph.Vertex>> rankingB,
                                                     final Set<Witness> witnessesB) {
        // Run the aligner.

        NeedlemanWunschGotohAligner aligner =
            new NeedlemanWunschGotohAligner(new NeedlemanWunschProfileScorer(scorer, witnessesA.size()));
        aligner.setDebugMatrix (debugMatrix);

        NeedlemanWunschScorerSetVertexSetVertex matcher =
            new NeedlemanWunschScorerSetVertexSetVertex(scorer);

        List<Pair<Set<VariantGraph.Vertex>, Set<VariantGraph.Vertex>>> alignmentList =
            aligner.align(rankingA, rankingB);

        // Build a new ranking by merging the aligner output into one ranking.
        List<Set<VariantGraph.Vertex>> collated = new ArrayList();
        for (Pair<Set<VariantGraph.Vertex>, Set<VariantGraph.Vertex>> alignment : alignmentList) {
            assert ((alignment.a != null) || (alignment.b != null));

            Set<VariantGraph.Vertex> verticesA = alignment.a;
            Set<VariantGraph.Vertex> verticesB = alignment.b;

            // Merge the matching vertices of each sequence
            NeedlemanWunschScorerSetVertexSetVertex.Match matching =
                matcher.match(verticesA, verticesB, matchScore);
            if (matching != null) {
                matching.vertexA.tokens().addAll(matching.vertexB.tokens());
                verticesB.remove(matching.vertexB);
            }

            Set<VariantGraph.Vertex> vertices = new HashSet<>();
            if (verticesA != null) {
                vertices.addAll(verticesA);
            }
            if (verticesB != null) {
                vertices.addAll(verticesB);
            }
            collated.add(vertices);
        }
        return collated;
    }

    /**
     * Create a graph out of a ranking.
     *
     * This is the inverse of {@code VariantGraphRanking.of}.
     *
     * @param graph    The graph
     * @param ranking  The ranking
     */
    private void rankingToGraph(final VariantGraph graph,
                                final List<Set<VariantGraph.Vertex>> ranking) {
        graph.init();

        // A map of witness -> last vertex with witness
        Map<Witness, VariantGraph.Vertex> vertexMap =
            getWitnesses(ranking).stream().collect(Collectors.toMap(w -> w, w -> graph.getStart()));

        for (Set<VariantGraph.Vertex> vertices : ranking) {
            // Normally, if we convert a graph to a table, and one path between
            // two vertices contains more vertices than another path, the
            // relation between vertices in the 'shorter' path and their ranks
            // will become ambiguous.  To avoid that, we insert placeholder
            // vertices, so that all witnesses connect to some vertex at each
            // rank and all paths will be of the same length.
            VariantGraph.Vertex placeholder = new VariantGraph.Vertex(graph);
            Set<Witness> unconnectedWitnesses = new HashSet<>(vertexMap.keySet());
            unconnectedWitnesses.removeAll(getWitnesses(Collections.singleton(vertices)));
            for (Witness w : unconnectedWitnesses) {
                graph.connect(vertexMap.put(w, placeholder), placeholder, Collections.singleton(w));
            }

            // Connect the 'real' vertices in each rank.
            for (VariantGraph.Vertex vertex : importVertexSet(graph, vertices)) {
                for (Witness w : getWitnesses(vertex)) {
                    graph.connect(vertexMap.put(w, vertex), vertex, Collections.singleton(w));
                }
            }
        }

        // Connect the end.
        for (VariantGraph.Vertex vertex : vertexMap.values()) {
            graph.connect(vertex, graph.getEnd(), getWitnesses(vertex));
        }
    }
}
