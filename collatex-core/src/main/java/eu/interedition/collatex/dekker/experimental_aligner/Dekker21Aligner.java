package eu.interedition.collatex.dekker.experimental_aligner;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.*;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.IslandConflictResolver;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;
import java.util.function.Predicate;

public class Dekker21Aligner extends CollationAlgorithm.Base {
    public TokenIndex tokenIndex;
    // tokens are mapped to vertices by their position in the token array
    protected VariantGraph.Vertex[] vertex_array;
    // map vertices to LCP
    // NOTE: vertices contain tokens... tokens are already mapped to LCP intervals
    // NOTE: It should be possible to remove this map

    // TODO: REMOVE REMOVE REMOVE!
    private Map<VariantGraph.Vertex, Block> vertexToLCP;
    // for debugging purposes only
    protected MatchTable table;
    protected List<Island> allIslands;
    protected List<Island> preferredIslands;
    protected List<List<Match>> transpositions;

    public Dekker21Aligner() {
        vertexToLCP = new HashMap<>();
    }

    @Override
    public void collate(VariantGraph graph, Iterable<Token> tokens) {
        throw new RuntimeException("Progressive alignment is not supported!");
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

            // System.out.println("Aligning next witness; Creating block based match table!");
            table = BlockBasedMatchTable.create(this, graph, tokens);

            // Phase 2: do the actual alignment and find transpositions
            IslandConflictResolver resolver = new IslandConflictResolver(table);
            preferredIslands = resolver.createNonConflictingVersion().getIslands();
            // we need to convert the islands into Map<Token, Vertex> for further processing
            // Here the result is put in a map
            Map<Token, VariantGraph.Vertex> alignments = new HashMap<>();
            for (Island island : preferredIslands) {
                for (Coordinate c : island) {
                    alignments.put(table.tokenAt(c.row, c.column), table.vertexAt(c.row, c.column));
                }
            }

            // detect phrases and transpositions
            // NOTE: It is probable that phrases can be replaced by Islands
            PhraseMatchDetector detector = new PhraseMatchDetector();
            List<List<Match>> phraseMatches = detector.detect(alignments, graph, tokens);

            TranspositionDetector detector2 = new TranspositionDetector();
            transpositions = detector2.detect(phraseMatches, graph);
            // Filter out transpositions from linked tokens
            for (List<Match> transposedPhrase : transpositions) {
                for (Match match : transposedPhrase) {
                    alignments.remove(match.token);
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
            mergeTranspositions(graph, transpositions);

            // we need to update the token -> vertex map
            // that information is stored in protected map
            int tokenPosition = tokenIndex.getStartTokenPositionForWitness(tokens.iterator().next().getWitness());
            for (Token token : tokens) {
                VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
                vertex_array[tokenPosition] = vertex;
                tokenPosition++;
            }
        }
    }


    public Set<Island> getIslands() {
//       return allIslands;
        return table.getIslands();
    }
}
