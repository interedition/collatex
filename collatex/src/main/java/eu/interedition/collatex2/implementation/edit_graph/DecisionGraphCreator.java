package eu.interedition.collatex2.implementation.edit_graph;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class DecisionGraphCreator {
  private final IVariantGraphMatcher matcher;
  private final IVariantGraph vGraph;
  private final IWitness b;

  //remove second and third parameter or the first one alone!
  public DecisionGraphCreator(IVariantGraphMatcher matcher, IVariantGraph vGraph, IWitness b) {
    this.matcher = matcher;
    this.vGraph = vGraph;
    this.b = b;
  }

  public DecisionGraph buildDecisionGraph() {
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(vGraph, b);
    // build the decision graph from the matches and the variant graph
    DecisionGraph dGraph = new DecisionGraph(vGraph.getStartVertex());
    Set<DGVertex> lastConstructedVertices = Sets.newLinkedHashSet();
    lastConstructedVertices.add(dGraph.getStartVertex());
    for (INormalizedToken wToken : b.getTokens()) {
      List<INormalizedToken> matchingTokens = matches.get(wToken);
      if (!matchingTokens.isEmpty()) {
        // Ik moet hier alle aangemaakte vertices in de DGraph opvangen
        Set<DGVertex> newConstructedVertices = Sets.newLinkedHashSet();
        for (INormalizedToken match : matchingTokens) {
          DGVertex dgVertex = new DGVertex(match);
          dGraph.add(dgVertex);
          newConstructedVertices.add(dgVertex);
          // TODO: you don't want to always draw an edge 
          // TODO: in the case of ngrams in witness and superbase
          // TODO: less edges are needed
          for (DGVertex lastVertex : lastConstructedVertices) {
            INormalizedToken lastToken = lastVertex.getToken();
            int gap = vGraph.isNear(lastToken, match) ?  0 : 1;
            dGraph.add(new DGEdge(lastVertex, dgVertex, gap));
          }
        }
        lastConstructedVertices = newConstructedVertices;
      }
    }
    for (DGVertex lastVertex : lastConstructedVertices) {
      INormalizedToken lastToken = lastVertex.getToken();
      int gap = vGraph.isNear(lastToken, vGraph.getEndVertex()) ?  0 : 1;
      dGraph.add(new DGEdge(lastVertex, dGraph.getEndVertex(), gap));
    }
    return dGraph;
  }
}
