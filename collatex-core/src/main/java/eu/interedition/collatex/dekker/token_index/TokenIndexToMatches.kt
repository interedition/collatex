package eu.interedition.collatex.dekker.token_index

import eu.interedition.collatex.Token
import eu.interedition.collatex.VariantGraph
import eu.interedition.collatex.dekker.Match
import eu.interedition.collatex.dekker.island.Coordinate
import eu.interedition.collatex.dekker.island.Island
import eu.interedition.collatex.util.VariantGraphRanking
import java.util.*
import java.util.stream.Collectors

/**
 * Created by ronald on 4/26/15.
 */
object TokenIndexToMatches {
    fun createMatches(tokenIndex: TokenIndex, vertex_array: Array<VariantGraph.Vertex?>, g: VariantGraph?, w: Iterable<Token>): Set<Island> {
        // we need the variant graph ranking for the projection in the vector space
        val ranking = VariantGraphRanking.of(g)
        // init result
        val result: MutableSet<Island> = HashSet()
        // based on the TokenIndex we build up the islands...
        // an island is a graph instance and a witness instance of the same block combined
        val witness = w.iterator().next().witness
        val startTokenPositionForWitness = tokenIndex.getStartTokenPositionForWitness(witness)
        val instances = tokenIndex.getBlockInstancesForWitness(witness)
        // we have to combine each instance in the witness with the other instances already present in the graph
        for (witnessInstance in instances) {
            // System.out.println("Debug creating matches for witness block instance: "+witnessInstance);
            // for every instance of a block in the witness we need to fetch the corresponding graph instances of the block
            // calculate graph block instances
            // fetch block
            val block = witnessInstance.block
            val allInstances = block.allInstances
            val graphInstances = allInstances.stream() //
                .filter { instance: Block.Instance -> instance.start_token < startTokenPositionForWitness } //
                .collect(Collectors.toList())
            // now for every graph block instance we have to create matches
            // for backwards compatibility reasons we do that with the Island and Coordinates classes
            for (graphInstance in graphInstances) {
                // we need to create an island for every block instance in the graph corresponding to this block instance in the witness
                val island = Island(witnessInstance)
                // for every matching token from the witness with a vertex in the graph we need to create a coordinate and
                // 1) add it to the island and 2) set the corresponding cell in the table
                // set the tokens and vertices on the table
                val graph_start_token = graphInstance.start_token
                for (i in 0 until block.length) {
                    val v = vertex_array[graph_start_token + i]
                        ?: throw RuntimeException("Vertex is null for token \"+graph_start_token+i+\" that is supposed to be mapped to a vertex in the graph!")
                    val column = ranking.apply(v) - 1
                    val witnessStartToken = witnessInstance.start_token + i
                    val row = witnessStartToken - startTokenPositionForWitness
                    // create coordinate and at it to the Island for the combination of graph block instance and witness block instance
                    // /*if (i == 0)*/ System.out.println("We go "+row + " "+column +" "+witnessStartToken);
                    val token = tokenIndex.token_array!![witnessStartToken]
                    val match = Match(v, token)
                    val coordinate = Coordinate(row, column, match)
                    island.add(coordinate)
                }
                result.add(island)
            }
        }
        return result
    }
}