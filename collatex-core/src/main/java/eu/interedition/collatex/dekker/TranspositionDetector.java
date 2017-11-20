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
package eu.interedition.collatex.dekker;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;

/**
 * @author Ronald Haentjens Dekker
 */
public class TranspositionDetector {
    private Map<List<Match>, Integer> phraseMatchToIndex;

    public List<List<Match>> detect(final List<List<Match>> phraseMatches, VariantGraph base) {
        // if there are no phrase matches it is not possible
        // to detect transpositions, return an empty list
        if (phraseMatches.isEmpty()) {
            return new ArrayList<>();
        }

    /*
     * We order the phrase matches in the topological order
     * of the graph (called rank). When the rank is equal
     * for two phrase matches, the witness order is used
     * to differentiate.
     */
        final VariantGraphRanking ranking = rankTheGraph(phraseMatches, base);

        Comparator<List<Match>> comp = (pm1, pm2) -> {
            int rank1 = ranking.apply(pm1.get(0).vertex);
            int rank2 = ranking.apply(pm2.get(0).vertex);
            int difference = rank1 - rank2;
            if (difference != 0) {
                return difference;
            }
            int index1 = phraseMatches.indexOf(pm1);
            int index2 = phraseMatches.indexOf(pm2);
            return index1 - index2;
        };

        List<List<Match>> phraseMatchesGraphOrder = new ArrayList<>(phraseMatches);
        phraseMatchesGraphOrder.sort(comp);

        // Map 1
        phraseMatchToIndex = new HashMap<>();
        for (int i = 0; i < phraseMatchesGraphOrder.size(); i++) {
            phraseMatchToIndex.put(phraseMatchesGraphOrder.get(i), i);
        }

    /*
     * We calculate the index for all the phrase matches
     * First in witness order, then in graph order
     */
        List<Integer> phraseMatchesGraphIndex = new ArrayList<>();
        List<Integer> phraseMatchesWitnessIndex = new ArrayList<>();

        for (int i = 0; i < phraseMatches.size(); i++) {
            phraseMatchesGraphIndex.add(i);
        }

        for (List<Match> phraseMatch : phraseMatches) {
            phraseMatchesWitnessIndex.add(phraseMatchToIndex.get(phraseMatch));
        }

    /*
     * Initialize result variables
     */
        List<List<Match>> nonTransposedPhraseMatches = new ArrayList<>(phraseMatches);
        List<List<Match>> transpositions = new ArrayList<>();

    /*
     * loop here until the maximum distance == 0
     */
        while (true) {
            // Map 2
            final Map<List<Match>, Integer> phraseMatchToDistanceMap = new LinkedHashMap<>();
            for (int i = 0; i < nonTransposedPhraseMatches.size(); i++) {
                Integer graphIndex = phraseMatchesGraphIndex.get(i);
                Integer witnessIndex = phraseMatchesWitnessIndex.get(i);
                Integer distance = Math.abs(graphIndex - witnessIndex);
                List<Match> phraseMatch = nonTransposedPhraseMatches.get(i);
                phraseMatchToDistanceMap.put(phraseMatch, distance);
            }

            List<Integer> distanceList = new ArrayList<>(phraseMatchToDistanceMap.values());

            if (distanceList.isEmpty() || Collections.max(distanceList) == 0) {
                break;
            }

            // sort phrase matches on distance, size
            // TODO: order by 3) graph rank?
            // TODO: I have not yet found evidence/a use case that
            // TODO: indicates that it is needed.
            Comparator<List<Match>> comp2 = (pm1, pm2) -> {
                // first order by distance
                int distance1 = phraseMatchToDistanceMap.get(pm1);
                int distance2 = phraseMatchToDistanceMap.get(pm2);
                int difference = distance2 - distance1;
                if (difference != 0) {
                    return difference;
                }
                // second order by size
                // return pm1.size() - pm2.size();
                return determineSize(pm1) - determineSize(pm2);
            };

            List<List<Match>> sortedPhraseMatches = new ArrayList<>(nonTransposedPhraseMatches);
            sortedPhraseMatches.sort(comp2);

            List<Match> transposedPhrase = sortedPhraseMatches.remove(0);

            Integer transposedIndex = phraseMatchToIndex.get(transposedPhrase);
            Integer graphIndex = phraseMatchesGraphIndex.indexOf(transposedIndex);
            Integer transposedWithIndex = phraseMatchesWitnessIndex.get(graphIndex);
            List<Match> linkedTransposedPhrase = phraseMatchesGraphOrder.get(transposedWithIndex);

            addTransposition(phraseMatchesWitnessIndex, phraseMatchesGraphIndex, nonTransposedPhraseMatches, transpositions, transposedPhrase);

            Integer distance = phraseMatchToDistanceMap.get(transposedPhrase);
            if (Objects.equals(distance, phraseMatchToDistanceMap.get(linkedTransposedPhrase)) && distance > 1) {
                addTransposition(phraseMatchesWitnessIndex, phraseMatchesGraphIndex, nonTransposedPhraseMatches, transpositions, linkedTransposedPhrase);
            }
        }
        return transpositions;
    }

    private void addTransposition(List<Integer> phraseWitnessRanks, List<Integer> phraseGraphRanks, List<List<Match>> nonTransposedPhraseMatches, List<List<Match>> transpositions, List<Match> transposedPhrase) {
        Integer indexToRemove = phraseMatchToIndex.get(transposedPhrase);
        nonTransposedPhraseMatches.remove(transposedPhrase);
        transpositions.add(transposedPhrase);
        phraseGraphRanks.remove(indexToRemove);
        phraseWitnessRanks.remove(indexToRemove);
    }

    private VariantGraphRanking rankTheGraph(List<List<Match>> phraseMatches, VariantGraph base) {
        // rank the variant graph
        Set<VariantGraph.Vertex> matchedVertices = new HashSet<>();
        for (List<Match> phraseMatch : phraseMatches) {
            matchedVertices.add(phraseMatch.get(0).vertex);
        }
        final VariantGraphRanking ranking = VariantGraphRanking.ofOnlyCertainVertices(base, matchedVertices);
        return ranking;
    }

    /*
     * in case of an a, b / b, a transposition we have to determine whether a or b
     * stays put. the phrase with the most character stays still if the tokens are
     * not simple tokens the phrase with the most tokens stays put
     */
    private int determineSize(List<Match> t) {
        Match firstMatch = t.get(0);
        if (!(firstMatch.token instanceof SimpleToken)) {
            return t.size();
        }
        int charLength = 0;
        for (Match m : t) {
            SimpleToken token = (SimpleToken) m.token;
            charLength += token.getNormalized().length();
        }
        return charLength;
    }
}
