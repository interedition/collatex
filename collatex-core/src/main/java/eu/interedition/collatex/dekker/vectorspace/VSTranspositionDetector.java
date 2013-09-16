package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.util.VariantGraphRanking;

// @author: Ronald Haentjens Dekker
// Experimental code!
// CODE IS NOT USED AT THE MOMENT
public class VSTranspositionDetector {

  public Stack<List<Match>> detectTranspositions(VariantGraph graph, List<List<Match>> phrases) {
    Set<VariantGraph.Vertex> firstVertices = Sets.newHashSet();
    for (List<Match> phrase : phrases) {
      firstVertices.add(phrase.get(0).vertex);
    }
    // prepare for transposition detection
    // rank vertices
    VariantGraphRanking ranking = VariantGraphRanking.ofOnlyCertainVertices(graph, null, firstVertices);
    // gather matched ranks into a list ordered by their natural order
    final List<Integer> phraseRanks = Lists.newArrayList();
    for (List<Match> phrase : phrases) {
      phraseRanks.add(Preconditions.checkNotNull(ranking.apply(phrase.get(0).vertex)));
    }
    Collections.sort(phraseRanks);
    // detect transpositions
    final Stack<List<Match>> transpositions = new Stack<List<Match>>();
    int previousRank = 0;
    for (List<Match> phrase: phrases) {
      int rank = ranking.apply(phrase.get(0).vertex);
      int expectedRank = phraseRanks.get(previousRank);
      if (expectedRank != rank) { 
        addNewTransposition(phrase, transpositions);
      }
      previousRank++;
    }
    return transpositions;
  }

  private void addNewTransposition(List<Match> phrase, Stack<List<Match>> transpositions) {
    //LOG.fine("Transposition found! "+phrase);
    transpositions.add(phrase);
  }

}
