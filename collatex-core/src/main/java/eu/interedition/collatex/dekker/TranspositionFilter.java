package eu.interedition.collatex.dekker;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;

public class TranspositionFilter {

  // we filter out small transposed phrases over large distances
  public static void filter(VariantGraph graph, List<List<Match>> transpositions, Map<Token, VariantGraph.Vertex> witnessTokenVertices) {
    List<List<Match>> falseTranspositions = Lists.newArrayList();
    
    VariantGraphRanking ranking = VariantGraphRanking.of(graph);
    
    for (List<Match> transposedPhrase : transpositions) {
      Match match = transposedPhrase.get(0);
      VariantGraph.Vertex v1 = witnessTokenVertices.get(match.token);
      VariantGraph.Vertex v2 = match.vertex;
      Integer rankingV1 = ranking.apply(v1);
      Integer rankingV2 = ranking.apply(v2);
      int distance = Math.abs(rankingV1-rankingV2)-1;
      if (distance > transposedPhrase.size()*3) {
        falseTranspositions.add(transposedPhrase);
      }
    }
  
    for (List<Match> transposition : falseTranspositions) {
      transpositions.remove(transposition);
    }
  }

}
