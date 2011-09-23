package eu.interedition.collatex2.implementation.edit_graph;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.matching.IVariantGraphMatcher;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class EditGraphCreator {
  private final IVariantGraphMatcher matcher;
  private final IVariantGraph vGraph;
  private final IWitness b;

  //remove second and third parameter or the first one alone!
  public EditGraphCreator(IVariantGraphMatcher matcher, IVariantGraph vGraph, IWitness b) {
    this.matcher = matcher;
    this.vGraph = vGraph;
    this.b = b;
  }

  public EditGraph buildDecisionGraph() {
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(vGraph, b);
    // build the decision graph from the matches and the variant graph
    EditGraph dGraph = new EditGraph(vGraph.getStartVertex());
    Set<EditGraphVertex> lastConstructedVertices = Sets.newLinkedHashSet();
    lastConstructedVertices.add(dGraph.getStartVertex());
    for (INormalizedToken wToken : b.getTokens()) {
      List<INormalizedToken> matchingTokens = matches.get(wToken);
      if (!matchingTokens.isEmpty()) {
        // Ik moet hier alle aangemaakte vertices in de DGraph opvangen
        Set<EditGraphVertex> newConstructedVertices = Sets.newLinkedHashSet();
        for (INormalizedToken match : matchingTokens) {
          EditGraphVertex dgVertex = new EditGraphVertex(match);
          dGraph.add(dgVertex);
          newConstructedVertices.add(dgVertex);
          // TODO: you don't want to always draw an edge 
          // TODO: in the case of ngrams in witness and superbase
          // TODO: less edges are needed
          for (EditGraphVertex lastVertex : lastConstructedVertices) {
            INormalizedToken lastToken = lastVertex.getToken();
            int gap = vGraph.isNear(lastToken, match) ?  0 : 1;
            dGraph.add(new EditGraphEdge(lastVertex, dgVertex, gap));
          }
        }
        lastConstructedVertices = newConstructedVertices;
      }
    }
    for (EditGraphVertex lastVertex : lastConstructedVertices) {
      INormalizedToken lastToken = lastVertex.getToken();
      int gap = vGraph.isNear(lastToken, vGraph.getEndVertex()) ?  0 : 1;
      dGraph.add(new EditGraphEdge(lastVertex, dGraph.getEndVertex(), gap));
    }
    return dGraph;
  }
}
