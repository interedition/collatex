package eu.interedition.collatex.dekker;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.IslandConflictResolver;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.simple.SimpleWitness;

import java.util.*;
import java.util.function.Predicate;

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

    public Dekker21Aligner(SimpleWitness[] w) {
        this.tokenIndex = new TokenIndex(w);
        tokenIndex.prepare();
        vertexToLCP = new HashMap<>();
        this.vertex_array = new VariantGraph.Vertex[tokenIndex.token_array.size()];
    }

    @Override
    public void collate(VariantGraph against, Iterable<Token> witness) {
        // first witness?
        boolean first_witness = vertexToLCP.isEmpty();
        if (first_witness) {
            super.merge(against, witness, new HashMap<>());
            // need to update vertex to lcp map

            // we need witness token -> vertex
            // that information is stored in protected map
            int tokenPosition = 0;
            for (Token token : witness) {
                VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
                Block interval = tokenIndex.getLCP_intervalFor(tokenPosition);
                vertexToLCP.put(vertex, interval);
                vertex_array[tokenPosition] = vertex;
                tokenPosition++;
            }
            return;
        }
        //TODO: handle next witness
        table = BlockBasedMatchTable.create(this, against, witness);
        IslandConflictResolver resolver = new IslandConflictResolver(table);
        preferredIslands = resolver.createNonConflictingVersion().getIslands();
        // we need to convert the islands into Map<Token, Vertex> for further processing
        // Here the result is put in a map
        Map<Token, VariantGraph.Vertex> linkedTokens = new HashMap<>();
        for (Island island : preferredIslands) {
            for (Coordinate c : island) {
                linkedTokens.put(table.tokenAt(c.row, c.column), table.vertexAt(c.row, c.column));
            }
        }
        System.out.println(linkedTokens);
        PhraseMatchDetector detector = new PhraseMatchDetector();
        List<List<Match>> phraseMatches = detector.detect(linkedTokens, against, witness);
        System.out.println(phraseMatches);
        TranspositionDetector detector2 = new TranspositionDetector();
        transpositions = detector2.detect(phraseMatches, against);
        // Filter out transpositions from linked tokens
        // and merge
    }

}
