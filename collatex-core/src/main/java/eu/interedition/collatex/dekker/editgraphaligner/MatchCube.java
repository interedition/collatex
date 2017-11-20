package eu.interedition.collatex.dekker.editgraphaligner;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.token_index.Block;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Ronald Haentjens Dekker on 08/01/17.
 * <p>
 * This class builds a cube of matches, given a VariantGraphRanking, a TokenComparator and the next witness.
 */
public class MatchCube {
    private final Map<MatchCoordinate, Match> matches = new HashMap<>();

    public MatchCube(TokenIndex tokenIndex, //
                     Iterable<Token> witnessTokens,//
                     VariantGraph.Vertex[] vertex_array, //
                     VariantGraphRanking variantGraphRanking) {

        Witness witness = witnessTokens.iterator().next().getWitness();
        int startTokenPositionForWitness = tokenIndex.getStartTokenPositionForWitness(witness);
        List<Block.Instance> instances = tokenIndex.getBlockInstancesForWitness(witness);
        for (Block.Instance witnessInstance : instances) {
            // System.out.println("Debug creating matches for witness block instance: "+witnessInstance);
            // for every instance of a block in the witness we need to fetch the corresponding graph instances of the block
            // calculate graph block instances
            // fetch block
            Block block = witnessInstance.block;
            List<Block.Instance> allInstances = block.getAllInstances();
            List<Block.Instance> graphInstances = allInstances.stream()//
                .filter(instance -> instance.start_token < startTokenPositionForWitness)//
                .collect(Collectors.toList());
            // now for every graph block instance we have to create matches
            for (Block.Instance graphInstance : graphInstances) {
                int graph_start_token = graphInstance.start_token;
                for (int i = 0; i < block.length; i++) {
                    VariantGraph.Vertex v = vertex_array[graph_start_token + i];
                    if (v == null) {
                        throw new RuntimeException("Vertex is null for token \"" + graph_start_token + i + "\" that is supposed to be mapped to a vertex in the graph!");
                    }
                    int rank = variantGraphRanking.apply(v) - 1;
                    int witnessStartToken = witnessInstance.start_token + i;
                    int row = witnessStartToken - startTokenPositionForWitness;
                    Token token = tokenIndex.token_array[witnessStartToken];
                    Match match = new Match(v, token);
                    MatchCoordinate coordinate = new MatchCoordinate(row, rank);
//                    System.out.println("match:[" + row + "," + rank + "]:" + token);
                    matches.put(coordinate, match);
                }
            }
        }
    }

    private boolean hasTokens(Vertex vertex) {
        return !vertex.tokens().isEmpty();
    }

    public boolean hasMatch(int y, int x) {
        MatchCoordinate c = new MatchCoordinate(y, x);
        return matches.containsKey(c);
    }

    public Match getMatch(int y, int x) {
        MatchCoordinate c = new MatchCoordinate(y, x);
        return matches.get(c);
    }

    class MatchCoordinate {
        final int tokenIndex; // position in witness, starting from zero
        final int rankInVG; // rank in the variant graph

        public MatchCoordinate(int tokenIndex, int rankInVG) {
            this.tokenIndex = tokenIndex;
            this.rankInVG = rankInVG;
        }

        @Override
        public int hashCode() {
            return tokenIndex * 72 + rankInVG;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MatchCoordinate)) {
                return false;
            }
            MatchCoordinate other = (MatchCoordinate) obj;
            return this.tokenIndex == other.tokenIndex && this.rankInVG == other.rankInVG;
        }
    }
}
