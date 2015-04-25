package eu.interedition.collatex.dekker;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.astar.AstarAlgorithm;
import eu.interedition.collatex.dekker.astar.Cost;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;
import eu.interedition.collatex.util.VariantGraphRanking;

public class Dekker21Aligner extends CollationAlgorithm.Base {

    protected TokenIndex tokenIndex;
    // tokens are mapped to vertices by their position in the token array
    protected VariantGraph.Vertex[] vertex_array;
    // map vertices to LCP
    // NOTE: vertices contain tokens... tokens are already mapped to LCP intervals
    // NOTE: It should be possible to remove this map
    private Map<VariantGraph.Vertex, LCP_Interval> vertexToLCP;

    public Dekker21Aligner(SimpleWitness[] w) {
        this.tokenIndex = new TokenIndex(this, w);
        tokenIndex.prepare();
        vertexToLCP = new HashMap<>();
        this.vertex_array = new VariantGraph.Vertex[tokenIndex.token_array.size()];
    }


    protected String debug(LCP_Interval interval) {
        return interval.toString() + " -> " + getNormalizedForm(interval);
    }

    protected String getNormalizedForm(LCP_Interval interval) {
        int suffix_start = interval.start;
        int token_pos = tokenIndex.suffix_array[suffix_start];
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < interval.length; i++) {
            Token t = tokenIndex.token_array.get(token_pos+i);
            tokens.add(t);
        }
        String normalized = "";
        for (Token t : tokens) {
            SimpleToken st = (SimpleToken) t;
            if (!normalized.isEmpty()) {
                normalized += " ";
            }
            normalized += st.getNormalized();
        }
        return normalized;
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
                LCP_Interval interval = tokenIndex.getLCP_intervalFor(tokenPosition);
                vertexToLCP.put(vertex, interval);
                vertex_array[tokenPosition] = vertex;
                tokenPosition++;
            }
            return;
        }
    }

    // lcp intervals can overlap horizontally
    // we prioritize the intervals with the biggest length
    // Note: with more than two witneses we have to select the right instance of an interval
    public List<LCP_Interval> getNonOverlappingBlocks() {
        // sort lcp intervals based on length in descending order
        Collections.sort(tokenIndex.lcp_intervals, (LCP_Interval interval1, LCP_Interval interval2) -> interval2.length - interval1.length);
        //TODO: set size based on the length of the token array
        BitSet occupied = new BitSet();
        // set up predicate
        // why is length check needed? empty lcp intervals should not be there
        Predicate<LCP_Interval> p = lcp_interval -> lcp_interval.length > 0 && !lcp_interval.getAllOccurrencesAsRanges(tokenIndex).anyMatch(i -> occupied.get(i));

        List<LCP_Interval> result = new ArrayList<>();
        for (LCP_Interval interval : tokenIndex.lcp_intervals) {
            // test whether the interval is in occupied
            //Note: filter
            if (p.test(interval)) {
                result.add(interval);
                // mark all the occurrences of the lcp interval in the occupied bit set
                interval.getAllOccurrencesAsRanges(tokenIndex).forEach(occupied::set);
            }
        }
        return result;
    }
}
