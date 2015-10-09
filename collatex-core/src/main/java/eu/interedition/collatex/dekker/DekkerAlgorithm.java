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

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.token_index.Block;
import eu.interedition.collatex.dekker.token_index.TokenIndexToMatches;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.dekker.island.*;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DekkerAlgorithm extends CollationAlgorithm.Base implements InspectableCollationAlgorithm {
    public TokenIndex tokenIndex;
    // tokens are mapped to vertices by their position in the token array
    protected VariantGraph.Vertex[] vertex_array;
    // map vertices to LCP
    // NOTE: vertices contain tokens... tokens are already mapped to LCP intervals
    // NOTE: It should be possible to remove this map

    // TODO: REMOVE REMOVE REMOVE!
    private Map<VariantGraph.Vertex, Block> vertexToLCP;
    // for debugging purposes only
    protected List<Island> preferredIslands;
    //TODO: FIX!
    private final Comparator<Token> comparator;
    private final PhraseMatchDetector phraseMatchDetector;
    private final TranspositionDetector transpositionDetector;
    private List<List<Match>> phraseMatches;
    private List<List<Match>> transpositions;
    private boolean mergeTranspositions = false;
    private Set<Island> allIslands;

    public DekkerAlgorithm() {
        this(new EqualityTokenComparator());
    }
    public DekkerAlgorithm(Comparator<Token> comparator) {
        this.comparator = comparator;
        this.phraseMatchDetector = new PhraseMatchDetector();
        this.transpositionDetector = new TranspositionDetector();
        //TODO: remove!
        vertexToLCP = new HashMap<>();
    }

    // The algorithm contains two phases:
    // 1) Matching phase
    // This phase is implemented using a token array -> suffix array -> LCP array -> LCP intervals
    //
    // 2) Alignment phase
    // This phase uses a decision tree (implemented as a table) to find the optimal alignment and moves
    @Override
    public void collate(VariantGraph graph, List<? extends Iterable<Token>> witnesses) {
        // matching phase
        this.tokenIndex = new TokenIndex(witnesses);
        tokenIndex.prepare();

        this.vertex_array = new VariantGraph.Vertex[tokenIndex.token_array.size()];

        for (Iterable<Token> tokens : witnesses) {
            // first witness?
            // TODO: WRONG!
            boolean first_witness = vertexToLCP.isEmpty();
            if (first_witness) {
                super.merge(graph, tokens, new HashMap<>());
                // need to update vertex to lcp map

                // we need tokens token -> vertex
                // that information is stored in protected map
                int tokenPosition = 0;
                for (Token token : tokens) {
                    VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
                    // remove
                    // Block interval = tokenIndex.getLCP_intervalFor(tokenPosition);
                    vertexToLCP.put(vertex, null);
                    // end remove
                    vertex_array[tokenPosition] = vertex;
                    tokenPosition++;
                }
                continue;
            }

            // align second + witness(es)
            final Witness witness = StreamSupport.stream(tokens.spliterator(), false)
                .findFirst()
                .map(Token::getWitness)
                .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "{0} + {1}: {2} vs. {3}", new Object[]{graph, witness, graph.vertices(), tokens});
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Gather matches between variant graph and witness from token index", new Object[]{graph, witness});
            }

            // System.out.println("Aligning next witness; Creating block based match table!");
            allIslands = TokenIndexToMatches.createMatches(tokenIndex, vertex_array, graph, tokens);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Aligning witness and graph", new Object[]{graph, witness});
            }

            // Phase 2: do the actual alignment and find transpositions
            IslandConflictResolver resolver = new IslandConflictResolver(allIslands);
            preferredIslands = resolver.createNonConflictingVersion().getIslands();
            // we need to convert the islands into Map<Token, Vertex> for further processing
            // Here the result is put in a map
            Map<Token, VariantGraph.Vertex> alignments = new HashMap<>();
            for (Island island : preferredIslands) {
                for (Coordinate c : island) {
                    alignments.put(c.match.token, c.match.vertex);
                }
            }

            if (LOG.isLoggable(Level.FINER)) {
                for (Map.Entry<Token, VariantGraph.Vertex> tokenLink : alignments.entrySet()) {
                    LOG.log(Level.FINER, "{0} + {1}: Aligned token (incl transposed): {2} = {3}", new Object[]{graph, witness, tokenLink.getValue(), tokenLink.getKey()});
                }
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Detect phrase matches", new Object[]{graph, witness});
            }

            // detect phrases and transpositions
            // NOTE: It is probable that phrases can be replaced by Islands
            phraseMatches = phraseMatchDetector.detect(alignments, graph, tokens);

            if (LOG.isLoggable(Level.FINER)) {
                for (List<Match> phraseMatch : phraseMatches) {
                    LOG.log(Level.FINER, "{0} + {1}: Phrase match: {2}", new Object[]{graph, witness, phraseMatch});
                }
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Detect transpositions", new Object[]{graph, witness});
            }
            transpositions = transpositionDetector.detect(phraseMatches, graph);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "transpositions:{0}", transpositions);
            }

            if (LOG.isLoggable(Level.FINER)) {
                for (List<Match> transposition : transpositions) {
                    LOG.log(Level.FINER, "{0} + {1}: Transposition: {2}", new Object[]{graph, witness, transposition});
                }
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Determine aligned tokens by filtering transpositions", new Object[]{graph, witness});
            }

            //TODO: this seems to be a duplicate from the action above!
            alignments = new HashMap<>();
            for (List<Match> phrase : phraseMatches) {
                for (Match match : phrase) {
                    alignments.put(match.token, match.vertex);
                }
            }

            // Filter out transpositions from linked tokens
            for (List<Match> transposedPhrase : transpositions) {
                for (Match match : transposedPhrase) {
                    alignments.remove(match.token);
                }
            }

            if (LOG.isLoggable(Level.FINER)) {
                for (Map.Entry<Token, VariantGraph.Vertex> alignment : alignments.entrySet()) {
                    LOG.log(Level.FINER, "{0} + {1}: Alignment: {2} = {3}", new Object[]{graph, witness, alignment.getValue(), alignment.getKey()});
                }
            }


            // and merge
            merge(graph, tokens, alignments);

            // we filter out small transposed phrases over large distances
            List<List<Match>> falseTranspositions = new ArrayList<>();

            // rank the variant graph
            VariantGraphRanking ranking = VariantGraphRanking.of(graph);

            for (List<Match> transposedPhrase : transpositions) {
                Match match = transposedPhrase.get(0);
                VariantGraph.Vertex v1 = witnessTokenVertices.get(match.token);
                VariantGraph.Vertex v2 = match.vertex;
                int distance = Math.abs(ranking.apply(v1) - ranking.apply(v2)) - 1;
                if (distance > transposedPhrase.size() * 3) {
                    falseTranspositions.add(transposedPhrase);
                }
            }

            for (List<Match> transposition : falseTranspositions) {
                transpositions.remove(transposition);
            }

            // merge transpositions
            if (mergeTranspositions) {
                mergeTranspositions(graph, transpositions);
            }

            // we need to update the token -> vertex map
            // that information is stored in protected map
            int tokenPosition = tokenIndex.getStartTokenPositionForWitness(tokens.iterator().next().getWitness());
            for (Token token : tokens) {
                VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
                vertex_array[tokenPosition] = vertex;
                tokenPosition++;
            }

            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "!{0}: {1}", new Object[]{graph, StreamSupport.stream(graph.vertices().spliterator(), false).map(Object::toString).collect(Collectors.joining(", "))});
            }
        }
    }

    @Override
    public void collate(VariantGraph graph, Iterable<Token> tokens) {
        throw new RuntimeException("Progressive alignment is not supported!");
    }

    @Override
    public List<List<Match>> getPhraseMatches() {
        return Collections.unmodifiableList(phraseMatches);
    }

    @Override
    public List<List<Match>> getTranspositions() {
        return Collections.unmodifiableList(transpositions);
    }


    //TODO; isn't this the same as getPhraseMatches?
    public Set<Island> getIslands() {
        return allIslands;
    }

    /*
     * This check disables transposition rendering in the variant
     * graph when the variant graph contains more then two witnesses.
     * Transposition detection is done in a progressive manner
     * (witness by witness). When viewing the resulting graph
     * containing the variation for all witnesses
     * the detected transpositions can look strange, since segments
     * may have split into smaller or larger parts.
     */
    @Override
    public void setMergeTranspositions(boolean b) {
        this.mergeTranspositions = b;
    }
}
