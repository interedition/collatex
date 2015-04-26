package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ronald on 4/26/15.
 */
public class BlockBasedMatchTable implements MatchTable {

    private final List<Island> islands;

    private BlockBasedMatchTable(List<Island> islands) {
        this.islands = islands;
    }

    public static MatchTable createMatchTable(Dekker21Aligner aligner, VariantGraph g, Witness w) {
        // based on the TokenIndex we build up the islands...
        // an island is a graph instance and a witness instance of the same block combined
        List<Block.Instance> instances = aligner.tokenIndex.getBlockInstancesForWitness(w);
        // we need the variant graph ranking for the projection in the vector space
        VariantGraphRanking ranking = VariantGraphRanking.of(g);
        // result
        List<Island> result = new ArrayList<>();
        // we have to combine each instance in the witness with the other instances already present in the graph
        for (Block.Instance witnessInstance : instances) {
            // fetch block
            Block block = witnessInstance.block;
            List<Block.Instance> allInstances = block.getAllInstances();
            // calc graph Instances
            List<Block.Instance> graphInstances = allInstances.stream().filter(instance -> instance.start_token != witnessInstance.start_token).collect(Collectors.toList());
            for (Block.Instance graphInstance : graphInstances) {
                // combine witness and graph instance into an island
                // project graph instance into vectorspace
                int start_token = graphInstance.start_token;
                VariantGraph.Vertex v = aligner.vertex_array[start_token];
                if (v==null) {
                    throw new RuntimeException("Vertex is null!");
                }
                Integer column = ranking.apply(v)-1;
                int row = witnessInstance.start_token - aligner.tokenIndex.getStartTokenPositionForWitness(w);
                Coordinate startCoordinate = new Coordinate(row, column);
                Coordinate endCoordinate = new Coordinate(row+block.length-1, column+block.length-1);
                Island island = new Island(startCoordinate, endCoordinate);
                result.add(island);
            }
        }
        BlockBasedMatchTable table = new BlockBasedMatchTable(result);
        return table;
    }

    @Override
    public VariantGraph.Vertex vertexAt(int rowIndex, int columnIndex) {
        return null;
    }

    @Override
    public Token tokenAt(int rowIndex, int columnIndex) {
        return null;
    }

    @Override
    public List<Token> rowList() {
        return null;
    }

    @Override
    public List<Integer> columnList() {
        return null;
    }

    @Override
    public Set<Island> getIslands() {
        return new HashSet<>(islands);
    }
}
