package eu.interedition.collatex.dekker;

import java.util.*;

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

}
