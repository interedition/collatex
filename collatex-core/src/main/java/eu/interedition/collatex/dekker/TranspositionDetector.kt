/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.interedition.collatex.dekker

import eu.interedition.collatex.VariantGraph
import eu.interedition.collatex.simple.SimpleToken
import eu.interedition.collatex.util.VariantGraphRanking
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap
import kotlin.math.abs

/**
 * @author Ronald Haentjens Dekker
 */
class TranspositionDetector {

    fun detect(phraseMatches: List<List<Match>>?, base: VariantGraph): MutableList<List<Match>> {
        // if there are no phrase matches it is not possible
        // to detect transpositions, return an empty list
        if (phraseMatches!!.isEmpty()) {
            return ArrayList()
        }

        /*
         * We order the phrase matches in the topological order
         * of the graph (called rank). When the rank is equal
         * for two phrase matches, the witness order is used
         * to differentiate.
         */
        val ranking = rankTheGraph(phraseMatches, base)

        val comp = Comparator { pm1:List<Match>, pm2: List<Match> ->
            val rank1 = ranking.apply(pm1[0].vertex)
            val rank2 = ranking.apply(pm2[0].vertex)
            val difference = rank1 - rank2
            when {
                difference != 0 -> difference
                else -> {
                    val index1 = phraseMatches.indexOf(pm1)
                    val index2 = phraseMatches.indexOf(pm2)
                    index1 - index2
                }
            }
        }

        val phraseMatchesGraphOrder: List<List<Match>> = phraseMatches.sortedWith(comp)

        // Map 1
        val phraseMatchToIndex: MutableMap<List<Match>, Int> = HashMap()
        for (i in phraseMatchesGraphOrder.indices) {
            phraseMatchToIndex[phraseMatchesGraphOrder[i]] = i
        }

        /*
         * We calculate the index for all the phrase matches
         * First in witness order, then in graph order
         */
        val phraseMatchesGraphIndex: MutableList<Int?> = ArrayList()
        val phraseMatchesWitnessIndex: MutableList<Int?> = ArrayList()
        for (i in phraseMatches.indices) {
            phraseMatchesGraphIndex.add(i)
        }
        for (phraseMatch in phraseMatches) {
            phraseMatchesWitnessIndex.add(phraseMatchToIndex[phraseMatch])
        }

        // DEBUG
        // println(phraseMatchesGraphIndex)
        // println(phraseMatchesWitnessIndex)

        /*
         * Initialize result variables
         */
        val nonTransposedPhraseMatches: MutableList<List<Match>> = ArrayList(phraseMatches)
        val transpositions: MutableList<List<Match>> = ArrayList()

        /*
         * loop here until the maximum distance == 0
         */
        while (true) {
            // Map 2
            val phraseMatchToDistanceMap: MutableMap<List<Match>, Int> = LinkedHashMap()
            for (i in nonTransposedPhraseMatches.indices) {
                val graphIndex = phraseMatchesGraphIndex[i]
                val witnessIndex = phraseMatchesWitnessIndex[i]
                val distance = abs(graphIndex!! - witnessIndex!!)
                val phraseMatch = nonTransposedPhraseMatches[i]
                phraseMatchToDistanceMap[phraseMatch] = distance
            }
            val distanceList: List<Int> = ArrayList(phraseMatchToDistanceMap.values)
            // DEBUG
            // println(distanceList)
            if (distanceList.isEmpty() || Collections.max(distanceList) == 0) {
                break
            }

            // sort phrase matches on distance, size
            // TODO: order by 3) graph rank?
            // TODO: I have not yet found evidence/a use case that
            // TODO: indicates that it is needed.
            val comp2 = Comparator { pm1: List<Match>, pm2: List<Match> ->
                // first order by distance
                val distance1 = phraseMatchToDistanceMap[pm1]!!
                val distance2 = phraseMatchToDistanceMap[pm2]!!
                val difference = distance2 - distance1
                when {
                    difference != 0 -> difference
                    else -> determineSize(pm1) - determineSize(pm2)
                }
            }
            val sortedPhraseMatches: MutableList<List<Match>> = ArrayList(nonTransposedPhraseMatches.sortedWith(comp2))
            val transposedPhrase: List<Match> = sortedPhraseMatches.removeAt(0)
            val transposedIndex = phraseMatchToIndex[transposedPhrase]
            val graphIndex = phraseMatchesGraphIndex.indexOf(transposedIndex)
            val transposedWithIndex = phraseMatchesWitnessIndex[graphIndex]
            val linkedTransposedPhrase = phraseMatchesGraphOrder[transposedWithIndex!!]
            addTransposition(phraseMatchToIndex, phraseMatchesWitnessIndex, phraseMatchesGraphIndex, nonTransposedPhraseMatches, transpositions, transposedPhrase)
            val distance = phraseMatchToDistanceMap[transposedPhrase]
            if (distance == phraseMatchToDistanceMap[linkedTransposedPhrase] && distance!! > 1) {
                addTransposition(phraseMatchToIndex, phraseMatchesWitnessIndex, phraseMatchesGraphIndex, nonTransposedPhraseMatches, transpositions, linkedTransposedPhrase)
            }
        }
        return transpositions
    }

    private fun addTransposition(phraseMatchToIndex: Map<List<Match>, Int>, phraseWitnessRanks: MutableList<Int?>, phraseGraphRanks: MutableList<Int?>, nonTransposedPhraseMatches: MutableList<List<Match>>, transpositions: MutableList<List<Match>>, transposedPhrase: List<Match>) {
        val indexToRemove = phraseMatchToIndex[transposedPhrase]
        nonTransposedPhraseMatches.remove(transposedPhrase)
        transpositions.add(transposedPhrase)
        phraseGraphRanks.remove(indexToRemove)
        phraseWitnessRanks.remove(indexToRemove)
    }

    private fun rankTheGraph(phraseMatches: List<List<Match>>, base: VariantGraph): VariantGraphRanking {
        // rank the variant graph
        val matchedVertices: MutableSet<VariantGraph.Vertex> = HashSet()
        for (phraseMatch in phraseMatches) {
            matchedVertices.add(phraseMatch[0].vertex)
        }
        return VariantGraphRanking.ofOnlyCertainVertices(base, matchedVertices)
    }

    /*
     * in case of an a, b / b, a transposition we have to determine whether a or b
     * stays put. the phrase with the most character stays still if the tokens are
     * not simple tokens the phrase with the most tokens stays put
     */
    private fun determineSize(t: List<Match>): Int {
        val firstMatch = t[0]
        if (firstMatch.token !is SimpleToken) {
            return t.size
        }
        var charLength = 0
        for (m in t) {
            val token = m.token as SimpleToken
            charLength += token.normalized.length
        }
        return charLength
    }
}