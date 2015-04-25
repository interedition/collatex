package eu.interedition.collatex.dekker;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
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
    }

    // lcp intervals can overlap horizontally
    // we prioritize the intervals with the biggest length
    // Note: with more than two witnesses we have to select the right instance of an interval
    public List<Block> getNonOverlappingBlocks() {
        // sort lcp intervals based on length in descending order
        Collections.sort(tokenIndex.blocks, (Block interval1, Block interval2) -> interval2.length - interval1.length);
        //TODO: set size based on the length of the token array
        BitSet occupied = new BitSet();
        // set up predicate
        // why is length check needed? empty lcp intervals should not be there
        Predicate<Block> p = lcp_interval -> lcp_interval.length > 0 && !lcp_interval.getAllOccurrencesAsRanges().anyMatch(i -> occupied.get(i));

        List<Block> result = new ArrayList<>();
        for (Block interval : tokenIndex.blocks) {
            // test whether the interval is in occupied
            //Note: filter
            if (p.test(interval)) {
                result.add(interval);
                // mark all the occurrences of the lcp interval in the occupied bit set
                interval.getAllOccurrencesAsRanges().forEach(occupied::set);
            }
        }
        return result;
    }
}
