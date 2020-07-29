package eu.interedition.collatex.dekker

import eu.interedition.collatex.Token
import eu.interedition.collatex.VariantGraph
import eu.interedition.collatex.dekker.token_index.TokenIndex
import eu.interedition.collatex.matching.EqualityTokenComparator
import eu.interedition.collatex.simple.SimpleWitness
import eu.interedition.collatex.util.StreamUtil
import java.util.logging.Level

/*
 *
 *
 */


// functions to create witnesses
val SIGLA: CharArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

fun createWitnesses(vararg contents: String): Array<SimpleWitness> {
//    org.junit.Assert.assertTrue("Not enough sigla", contents.size <= SIGLA.size)
    val witnesses = arrayOf<SimpleWitness>()
    for (wc in 0 until contents.size) {
        witnesses[wc] = SimpleWitness(Character.toString(SIGLA.get(wc)), contents[wc])
    }
    return witnesses
}

// function to merge witness tokens into a variant graph based on a mapping of tokens -> Vertex
// returns a token -> vertex mapping for every token in the witness; Meaning there could be vertices added to graph for
// non aligned tokens.
fun merge(into: VariantGraph, witnessTokens: Iterable<Token>, alignments: Map<Token?, VariantGraph.Vertex>): HashMap<Token, VariantGraph.Vertex> {
    val witness = StreamUtil.stream(witnessTokens)
        .findFirst()
        .map { obj: Token -> obj.witness }
        .orElseThrow { IllegalArgumentException("Empty witness") }
//    if (LOG.isLoggable(Level.FINE)) {
//        LOG.log(Level.FINE, "{0} + {1}: Merge comparand into graph", arrayOf(into, witness))
//    }
    val witnessTokenVertices = HashMap<Token, VariantGraph.Vertex>()
    var last = into.start
    val witnessSet = setOf(witness)
    for (token in witnessTokens) {
        var matchingVertex = alignments[token]
        if (matchingVertex == null) {
            matchingVertex = into.add(token)
        } else {
//            if (LOG.isLoggable(Level.FINE)) {
//                LOG.log(Level.FINE, "Match: {0} to {1}", arrayOf<Any?>(matchingVertex, token))
//            }
            matchingVertex.add(setOf(token))
        }
        witnessTokenVertices.put(token, matchingVertex!!)
        into.connect(last, matchingVertex, witnessSet)
        last = matchingVertex
    }
    into.connect(last, into.end, witnessSet)
    return witnessTokenVertices
}


/*
 * We need test a lot of simple combinations
 * One without repetitions.
 * One with repetitions.
 *
 */

fun testNewAlgorithm() {
    // val w: Array<SimpleWitness> = createWitnesses("The same stuff", "The same stuff")
    val w2: Array<SimpleWitness> = createWitnesses("The black cat and the red cat", "the red cat")

    // Create the TokenIndex of the witnesses
    val index: TokenIndex = TokenIndex(EqualityTokenComparator(), w2[0], w2[1])
    index.prepare()

    // the first witness is different...
    // creating a variant graph is just a matter of adding nodes for each token
    val graph = VariantGraph()
    val tokens = w2[0] // first witness

    // merge in the first witness in the variant graph with an "empty" alignment.
    merge(graph, tokens, emptyMap())
    // updateTokenToVertexArray(tokens, witness)


    // val islands = TokenIndexToMatches.createMatches(index, )
}