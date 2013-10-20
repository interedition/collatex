/*
 * Copyright (c) 2013 The Interedition Development Group.
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.util.VariantGraphRanking;

/**
 * 
 * @author Ronald Haentjens Dekker
 */
public class TranspositionDetector {

  private static final Logger LOG = Logger.getLogger(TranspositionDetector.class.getName());
  private BiMap<List<Match>, Integer> phraseMatchToRank;

  public List<List<Match>> detect(List<List<Match>> phraseMatches, VariantGraph base) {
    // if there are no phrase matches it is not possible
    // to detect transpositions, return an empty list
    if (phraseMatches.isEmpty()) {
      return Lists.newArrayList();
    }

    // we moeten de ranks van de phrase matches bepalen
    // zowel in de graph als in de witness
    VariantGraphRanking ranking = rankTheGraph(phraseMatches, base);
    List<Integer> phraseWitnessRanks = getRankingForPhraseMatchesWitnessOrder(phraseMatches, ranking);
    List<Integer> phraseGraphRanks = maskGraphRanks(phraseWitnessRanks);

    // NOTE: Multiple phrase matches can have to same rank..
    // For the transposition detection we can work our way
    // around it
    // TODO: deal with it!

    // Map 0
    phraseMatchToRank = HashBiMap.create();
    for (int i = 0; i < phraseWitnessRanks.size(); i++) {
      // System.out.println("Trying to put: "+phraseMatches.get(i)+":"+phraseWitnessRanks.get(i));
      phraseMatchToRank.forcePut(phraseMatches.get(i), phraseWitnessRanks.get(i));
    }

    /*
     * Initialize result variables
     */

    List<List<Match>> nonTransposedPhraseMatches = Lists.newArrayList(phraseMatches);
    List<List<Match>> transpositions = Lists.newArrayList();

    /*
     * Hier gaan we loopen
     */
    while (true) {
      System.out.println(phraseGraphRanks);
      System.out.println(phraseWitnessRanks);

      // Map 1
      Map<Integer, Integer> graphRankToWitnessRankMap = Maps.newLinkedHashMap();
      for (Integer rank : phraseGraphRanks) {
        Integer graphRankIndex = phraseGraphRanks.indexOf(rank);
        Integer witnessRank = phraseWitnessRanks.get(graphRankIndex);
        graphRankToWitnessRankMap.put(rank, witnessRank);
      }
      // System.out.println(graphRankToWitnessRankMap);

      // Map 2
      final Map<Integer, Integer> rankToDistanceMap = Maps.newLinkedHashMap();
      for (Integer rank : phraseGraphRanks) {
        Integer graphRankIndex = phraseGraphRanks.indexOf(rank);
        Integer witnessRankIndex = phraseWitnessRanks.indexOf(rank);
        Integer distance = Math.abs(graphRankIndex - witnessRankIndex);
        rankToDistanceMap.put(rank, distance);
      }
      // System.out.println(rankToDistanceMap);

      List<Integer> distanceList = Lists.newArrayList(rankToDistanceMap.values());
      System.out.println(distanceList);

      if (Collections.max(distanceList) == 0) {
        break;
      }

      // sort phrase matches on distance
      Comparator<List<Match>> comp = new Comparator<List<Match>>() {
        @Override
        public int compare(List<Match> pm1, List<Match> pm2) {
          // first order by distance
          int distance1 = rankToDistanceMap.get(phraseMatchToRank.get(pm1));
          int distance2 = rankToDistanceMap.get(phraseMatchToRank.get(pm2));
          int difference = distance2 - distance1;
          if (difference != 0) {
            return difference;
          }
          // second order by size
          // TODO: order by 3) graph rank
          return pm1.size() - pm2.size();
        }
      };

      List<List<Match>> sortedPhraseMatches = Lists.newArrayList(nonTransposedPhraseMatches);
      Collections.sort(sortedPhraseMatches, comp);
      System.out.println(sortedPhraseMatches);

      List<Match> transposedPhrase = sortedPhraseMatches.remove(0);
      System.out.println(transposedPhrase);

      addTransposition(phraseWitnessRanks, phraseGraphRanks, nonTransposedPhraseMatches, transpositions, transposedPhrase);

      Integer transposedRank = phraseMatchToRank.get(transposedPhrase);
      Integer transposedWithRank = graphRankToWitnessRankMap.get(transposedRank);

      Integer distance = rankToDistanceMap.get(transposedRank);
      // System.out.println(transposedWithRank+":"+distance);
      if (distance == rankToDistanceMap.get(transposedWithRank) && distance > 1) {
        System.out.println("We need to also remove rank: " + transposedWithRank);
        List<Match> linkedTransposedPhrase = phraseMatchToRank.inverse().get(transposedWithRank);
        addTransposition(phraseWitnessRanks, phraseGraphRanks, nonTransposedPhraseMatches, transpositions, linkedTransposedPhrase);
      }
    }
    return transpositions;
  }

  private void addTransposition(List<Integer> phraseWitnessRanks, List<Integer> phraseGraphRanks, List<List<Match>> nonTransposedPhraseMatches, List<List<Match>> transpositions, List<Match> transposedPhrase) {
    Integer rankToRemove = phraseMatchToRank.get(transposedPhrase);
    System.out.println("Removing rank:" + rankToRemove);
    nonTransposedPhraseMatches.remove(transposedPhrase);
    transpositions.add(transposedPhrase);
    phraseGraphRanks.remove(rankToRemove);
    phraseWitnessRanks.remove(rankToRemove);
//    System.out.println(phraseGraphRanks);
//    System.out.println(phraseWitnessRanks);
  }

  private List<Integer> maskGraphRanks(List<Integer> maskedWitnessRanks) {
    List<Integer> maskedGraphRanks = Lists.newArrayList();
    maskedGraphRanks.addAll(maskedWitnessRanks);
    Collections.sort(maskedGraphRanks);
    return maskedGraphRanks;
  }

  private VariantGraphRanking rankTheGraph(List<List<Match>> phraseMatches, VariantGraph base) {
    // rank the variant graph
    Set<VariantGraph.Vertex> matchedVertices = Sets.newHashSet();
    for (List<Match> phraseMatch : phraseMatches) {
      matchedVertices.add(phraseMatch.get(0).vertex);
    }
    final VariantGraphRanking ranking = VariantGraphRanking.ofOnlyCertainVertices(base, null, matchedVertices);
    return ranking;
  }

  private List<Integer> getRankingForPhraseMatchesWitnessOrder(List<List<Match>> phraseMatches, final VariantGraphRanking ranking) {
    // gather matched ranks into a list ordered by their natural order
    final List<Integer> phraseWitnessRanks = Lists.newArrayList();
    for (List<Match> phraseMatch : phraseMatches) {
      phraseWitnessRanks.add(Preconditions.checkNotNull(ranking.apply(phraseMatch.get(0).vertex)));
    }
    return phraseWitnessRanks;
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

  private void logTranspositions(final Stack<List<Match>> transpositions) {
    if (LOG.isLoggable(Level.FINER)) {
      for (List<Match> transposition : transpositions) {
        LOG.log(Level.FINER, "Detected transposition: {0}", Iterables.toString(transposition));
      }
    }
  }
}
