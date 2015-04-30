package eu.interedition.collatex.dekker;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.IslandConflictResolver;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;

public class Dekker21Aligner extends CollationAlgorithm.Base {
    protected TokenIndex tokenIndex;
    // tokens are mapped to vertices by their position in the token array
    protected VariantGraph.Vertex[] vertex_array;
    // map vertices to LCP
    // NOTE: vertices contain tokens... tokens are already mapped to LCP intervals
    // NOTE: It should be possible to remove this map
    private Map<VariantGraph.Vertex, Block> vertexToLCP;
    // for debugging purposes only
    protected MatchTable table;
    protected List<Island> preferredIslands;
    protected List<List<Match>> transpositions;

    public Dekker21Aligner() {
        vertexToLCP = new HashMap<>();
    }

    @Override
    public void collate(VariantGraph graph, Iterable<Token> tokens) {
        throw new RuntimeException("Progressive alignment is not supported!");
    }

    @Override
    public void collate(VariantGraph graph, List<? extends Iterable<Token>> witnesses) {
        this.tokenIndex = new TokenIndex(witnesses);
        tokenIndex.prepare();

        this.vertex_array = new VariantGraph.Vertex[tokenIndex.token_array.size()];

        for (Iterable<Token> tokens : witnesses) {
            // first witness?
            boolean first_witness = vertexToLCP.isEmpty();
            if (first_witness) {
                super.merge(graph, tokens, new HashMap<>());
                // need to update vertex to lcp map

                // we need tokens token -> vertex
                // that information is stored in protected map
                int tokenPosition = 0;
                for (Token token : tokens) {
                    VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
                    Block interval = tokenIndex.getLCP_intervalFor(tokenPosition);
                    vertexToLCP.put(vertex, interval);
                    vertex_array[tokenPosition] = vertex;
                    tokenPosition++;
                }
                continue;
            }
            table = BlockBasedMatchTable.create(this, graph, tokens);
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
        }
    }


}
