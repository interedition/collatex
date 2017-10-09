package eu.interedition.collatex.dekker.token_index;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.island.Coordinate;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ronald on 4/26/15.
 */
public class TokenIndexToMatches {

    public static Set<Island> createMatches(TokenIndex tokenIndex, VariantGraph.Vertex[] vertex_array, VariantGraph g, Iterable<Token> w) {
        // we need the variant graph ranking for the projection in the vector space
        VariantGraphRanking ranking = VariantGraphRanking.of(g);
        // init result
        Set<Island> result = new HashSet<>();
        // based on the TokenIndex we build up the islands...
        // an island is a graph instance and a witness instance of the same block combined
        Witness witness = w.iterator().next().getWitness();
        int startTokenPositionForWitness = tokenIndex.getStartTokenPositionForWitness(witness);
        List<Block.Instance> instances = tokenIndex.getBlockInstancesForWitness(witness);
        // we have to combine each instance in the witness with the other instances already present in the graph
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
            // for backwards compatibility reasons we do that with the Island and Coordinates classes
            for (Block.Instance graphInstance : graphInstances) {
                // we need to create an island for every block instance in the graph corresponding to this block instance in the witness
                Island island = new Island(witnessInstance);
                // for every matching token from the witness with a vertex in the graph we need to create a coordinate and
                // 1) add it to the island and 2) set the corresponding cell in the table
                // set the tokens and vertices on the table
                int graph_start_token = graphInstance.start_token;
                for (int i = 0; i < block.length; i++) {
                    VariantGraph.Vertex v = vertex_array[graph_start_token + i];
                    if (v == null) {
                        throw new RuntimeException("Vertex is null for token \"+graph_start_token+i+\" that is supposed to be mapped to a vertex in the graph!");
                    }
                    int column = ranking.apply(v) - 1;
                    int witnessStartToken = witnessInstance.start_token + i;
                    int row = witnessStartToken - startTokenPositionForWitness;
                    // create coordinate and at it to the Island for the combination of graph block instance and witness block instance
                    // /*if (i == 0)*/ System.out.println("We go "+row + " "+column +" "+witnessStartToken);
                    Token token = tokenIndex.token_array[witnessStartToken];
                    Match match = new Match(v, token);
                    Coordinate coordinate = new Coordinate(row, column, match);
                    island.add(coordinate);
                }
                result.add(island);
            }
        }
        return result;
    }
}
