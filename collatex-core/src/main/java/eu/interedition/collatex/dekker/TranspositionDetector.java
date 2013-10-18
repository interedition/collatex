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
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
    
    // dan moeten we de phrasematches op size sorteren
    // (Let daarbij op het Greek example van Troy)
    List<List<Match>> sortedPhraseMatches = sortPhraseMatchesBySizeLargestFirst(phraseMatches, base);
    
    // we have to find the largest non transposed phrase match
    // if, on first view, there are no non transposed phrase matches we take the largest transposed phrase match
    
    // dan moeten we een verzameling aanmaken met
    // niet getranspositieneerde phrases
    List<List<Match>> nonTransposedPhrases = getNonTransposedPhraseMatches(phraseWitnessRanks, phraseMatches);
    
    if (nonTransposedPhrases.isEmpty()) {
      nonTransposedPhrases.add(sortedPhraseMatches.remove(0));
    }

    //NOTE: we zouden eigenlijk de nonTransposedPhrases uit de 
    //sortedphrasematches moeten halen (voor perf reasons).
    
    List<List<Match>> transpositions = Lists.newArrayList();

    while(!sortedPhraseMatches.isEmpty()) {
      List<Match> candidate = sortedPhraseMatches.remove(0);
      boolean transposed = isCandidateTransposed(phraseMatches, phraseWitnessRanks, nonTransposedPhrases, candidate);
      if (!transposed) {
        nonTransposedPhrases.add(candidate);
      } else {
        transpositions.add(candidate);
      }
    }
    return transpositions;
  }

  private List<List<Match>> getNonTransposedPhraseMatches(List<Integer> phraseWitnessRanks, List<List<Match>> phraseMatches) {
    List<Integer> phraseGraphRanks = Lists.newArrayList(phraseWitnessRanks);
    Collections.sort(phraseGraphRanks);
    
    List<List<Match>> nonTransposedPhraseMatches = Lists.newArrayList();
    
    for (int i=0; i< phraseGraphRanks.size(); i++) {
      if (phraseGraphRanks.get(i)==phraseWitnessRanks.get(i)) {
        nonTransposedPhraseMatches.add(phraseMatches.get(i));
      }
    }
    return nonTransposedPhraseMatches;
  }

  private boolean isCandidateTransposed(List<List<Match>> phraseMatches, List<Integer> phraseWitnessRanks, List<List<Match>> nonTransposedPhrases, List<Match> candidate) {
    List<List<Match>> phrasesToMask = Lists.newArrayList(nonTransposedPhrases);
    phrasesToMask.add(candidate);
    
    // daarna moeten we de integer lijsten masken
    // met alleen diegene die niet getranspositioneerd bleken
    List<Integer> maskedWitnessRanks = maskWitnessRanks(phraseMatches, phraseWitnessRanks, phrasesToMask);

    // nu de graph ranks nog doen
    List<Integer> maskedGraphRanks = maskGraphRanks(maskedWitnessRanks);
    
    // nu gaan we de masked ranks vergelijken...
    return areMaskedRanksTransposed(maskedWitnessRanks, maskedGraphRanks);
  }

  private boolean areMaskedRanksTransposed(List<Integer> maskedWitnessRanks, List<Integer> maskedGraphRanks) {
    boolean transposed = false;
    for (int i=0; i < maskedWitnessRanks.size(); i++) {
      if (maskedGraphRanks.get(i)!=maskedWitnessRanks.get(i)) {
        transposed=true;
        break;
      }
    }
    return transposed;
  }

  private List<Integer> maskGraphRanks(List<Integer> maskedWitnessRanks) {
    List<Integer> maskedGraphRanks = Lists.newArrayList();
    maskedGraphRanks.addAll(maskedWitnessRanks);
    Collections.sort(maskedGraphRanks);
    return maskedGraphRanks;
  }

  private List<Integer> maskWitnessRanks(List<List<Match>> phraseMatches, List<Integer> phraseWitnessRanks, List<List<Match>> phrasesToMask) {
    List<Integer> maskedWitnessRanks = Lists.newArrayList();
    for (int i=0; i < phraseMatches.size(); i++) {
      List<Match> pm = phraseMatches.get(i);
      if (phrasesToMask.contains(pm)) {
        int witnessRank = phraseWitnessRanks.get(i);
        maskedWitnessRanks.add(witnessRank);
      }
    }
    return maskedWitnessRanks;
  }

  /*
   * At first glance all the phrases seemed to have moved
   * As the first phrase to lock down we choose 
   * the one with the greatest size
   * and if that is not unique the one with
   * the lowest ranked phrase in the graph
   */
  private List<List<Match>> sortPhraseMatchesBySizeLargestFirst(List<List<Match>> phraseMatches, VariantGraph graph) {
    List<List<Match>> sortedPhraseMatches = Lists.newArrayList(phraseMatches);
    //NOTE: ranking is calculated twice in the TranspositionDetector class
    final VariantGraphRanking ranking = rankTheGraph(phraseMatches, graph);
    Collections.sort(sortedPhraseMatches, new Comparator<List<Match>>() {
      @Override
      public int compare(List<Match> pm1, List<Match> pm2) {
        // first compare phrase match size
        int result = pm2.size() - pm1.size();
        if (result != 0) {
          return result;
        }
        // second compare rank in graph difference
        int rank1 = ranking.apply(pm1.get(0).vertex);
        int rank2 = ranking.apply(pm2.get(0).vertex);
        return rank1 - rank2;
      }
    });
    return sortedPhraseMatches;
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
