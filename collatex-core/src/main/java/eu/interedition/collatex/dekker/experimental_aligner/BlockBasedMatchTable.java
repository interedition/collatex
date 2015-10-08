package eu.interedition.collatex.dekker.experimental_aligner;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Created by ronald on 4/26/15.
 */
public class BlockBasedMatchTable implements MatchTable {
    private final List<Island> islands;
    // NOTE: The table is sparse; so this is not the most efficient implementation.
    // Note: In theory would could move all the lookup functionality to the islands
    // Note: This would however introduce API changes
    private final MatchTableCell[][] table;
    private final Token[] witness;
    private final int[] ranks;

    private BlockBasedMatchTable(List<Island> islands, Token[] tokens, int[] ranks) {
        this.islands = islands;
        this.table = new MatchTableCell[tokens.length][ranks.length];
        this.witness = tokens;
        this.ranks = ranks;
    }

    public static MatchTable create(TokenIndex tokenIndex, VariantGraph.Vertex[] vertex_array, VariantGraph graph, Iterable<Token> witness) {
        return createMatchTable(tokenIndex, vertex_array, graph, witness);
    }

    public static MatchTable createMatchTable(TokenIndex tokenIndex, VariantGraph.Vertex[] vertex_array, VariantGraph g, Iterable<Token> w) {
        // we need the variant graph ranking for the projection in the vector space
        VariantGraphRanking ranking = VariantGraphRanking.of(g);
        // result
        List<Island> result = new ArrayList<>();
        // witness tokens
        Token[] tokens = StreamSupport.stream(w.spliterator(), false).toArray(Token[]::new);
        // graph ranks
        int[] ranks = IntStream.range(0, Math.max(0, ranking.apply(g.getEnd()) - 1)).toArray();
        // create table
        BlockBasedMatchTable table = new BlockBasedMatchTable(result, tokens, ranks);
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
            List<Block.Instance> graphInstances = allInstances.stream().filter(instance -> instance.start_token < startTokenPositionForWitness).collect(Collectors.toList());
            // now for every graph block instance we have to create matches
            // for backwards compatibility reasons we do that with the Island and Coordinates classes
            for (Block.Instance graphInstance : graphInstances) {
                // we need to create an island for every block instance in the graph corresponding to this block instance in the witness
                Island island = new Island();
                // for every matching token from the witness with a vertex in the graph we need to create a coordinate and
                // 1) add it to the island and 2) set the corresponding cell in the table
                // set the tokens and vertices on the table
                int graph_start_token = graphInstance.start_token;
                for (int i = 0; i < block.length; i++) {
                    VariantGraph.Vertex v = vertex_array[graph_start_token+i];
                    if (v==null) {
                        throw new RuntimeException("Vertex is null for token \"+graph_start_token+i+\" that is supposed to be mapped to a vertex in the graph!");
                    }
                    int column = ranking.apply(v)-1;
                    int witnessStartToken = witnessInstance.start_token + i;
                    int row = witnessStartToken - startTokenPositionForWitness;
                    // create coordinate and at it to the Island for the combination of graph block instance and witness block instance
                    // /*if (i == 0)*/ System.out.println("We go "+row + " "+column +" "+witnessStartToken);
                    Coordinate coordinate = new Coordinate(row, column);
                    island.add(coordinate);
                    // set vertex and token combination as a cell on the table
                    table.set(row, column, tokenIndex.token_array.get(witnessStartToken), v);
                }
                result.add(island);
            }
        }
        return table;
    }

    @Override
    public VariantGraph.Vertex vertexAt(int rowIndex, int columnIndex) {
        return cell(rowIndex, columnIndex).map(c -> c.vertex).orElseThrow(RuntimeException::new);
    }

    @Override
    public Token tokenAt(int rowIndex, int columnIndex) {
        return cell(rowIndex, columnIndex).map(c -> c.token).orElseThrow(RuntimeException::new);
    }

    private Optional<MatchTableCell> cell(int rowIndex, int columnIndex) {
        return Optional.ofNullable(table[rowIndex][columnIndex]);
    }

    private void set(int rowIndex, int columnIndex, Token token, VariantGraph.Vertex vertex) {
        table[rowIndex][columnIndex] = new MatchTableCell(token, vertex);
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

    private class MatchTableCell {
        public final Token token;
        public final VariantGraph.Vertex vertex;

        public MatchTableCell(Token token, VariantGraph.Vertex vertex) {
            this.token = token;
            this.vertex = vertex;
        }
    }
}
