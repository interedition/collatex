package eu.interedition.collatex2.implementation.edit_graph;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.containers.witness.FakeWitness;
import eu.interedition.collatex2.implementation.matching.IVariantGraphMatcher;
import eu.interedition.collatex2.implementation.matching.TokenMatcher;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class EditGraphCreator {
  private final IVariantGraphMatcher matcher;
  private final IVariantGraph vGraph;
  private final IWitness b;
  private final EditGraph editGraph;

  public EditGraphCreator(IVariantGraphMatcher matcher, IVariantGraph vGraph, IWitness b) {
    this(new EditGraph(vGraph.getStartVertex()), matcher, vGraph, b);
  }

  //remove second and third parameter or the first one alone!
  public EditGraphCreator(EditGraph editGraph, IVariantGraphMatcher matcher2, IVariantGraph vGraph2, IWitness b2) {
    this.editGraph = editGraph;
    this.matcher = matcher2;
    this.vGraph = vGraph2;
    this.b = b2;
  }

  public EditGraph buildEditGraph() {
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(vGraph, b);
    // build the decision graph from the matches and the variant graph
    Set<EditGraphVertex> lastConstructedVertices = Sets.newLinkedHashSet();
    lastConstructedVertices.add(editGraph.getStartVertex());
    for (INormalizedToken wToken : b.getTokens()) {
      List<INormalizedToken> matchingTokens = matches.get(wToken);
      if (!matchingTokens.isEmpty()) {
        // Ik moet hier alle aangemaakte vertices in de DGraph opvangen
        Set<EditGraphVertex> newConstructedVertices = Sets.newLinkedHashSet();
        for (INormalizedToken match : matchingTokens) {
          EditGraphVertex dgVertex = new EditGraphVertex(wToken, match);
          editGraph.add(dgVertex);
          newConstructedVertices.add(dgVertex);
          // TODO: you don't want to always draw an edge 
          // TODO: in the case of ngrams in witness and superbase
          // TODO: less edges are needed
          for (EditGraphVertex lastVertex : lastConstructedVertices) {
            INormalizedToken lastToken = lastVertex.getBaseToken();
            int gap = vGraph.isNear(lastToken, match) ?  0 : 1;
            editGraph.add(new EditGraphEdge(lastVertex, dgVertex, gap));
          }
        }
        lastConstructedVertices = newConstructedVertices;
      }
    }
    for (EditGraphVertex lastVertex : lastConstructedVertices) {
      INormalizedToken lastToken = lastVertex.getBaseToken();
      int gap = vGraph.isNear(lastToken, vGraph.getEndVertex()) ?  0 : 1;
      editGraph.add(new EditGraphEdge(lastVertex, editGraph.getEndVertex(), gap));
    }
    return editGraph;
  }

  // proberen we het hier nog eens
  public void buildEditGraph(FakeWitness base, FakeWitness witness) {
    TokenMatcher matcher = new TokenMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(base, witness);
    // build the decision graph from the matches and the variant graph
    Set<EditGraphVertex> lastConstructedVertices = Sets.newLinkedHashSet();
    lastConstructedVertices.add(editGraph.getStartVertex());
    for (INormalizedToken wToken : witness.getTokens()) {
      List<INormalizedToken> matchingTokens = matches.get(wToken);
      if (!matchingTokens.isEmpty()) {
        Set<EditGraphVertex> newConstructedVertices = Sets.newLinkedHashSet();
        for (INormalizedToken match : matchingTokens) {
          EditGraphVertex editGraphVertex = new EditGraphVertex(wToken, match);
          editGraph.add(editGraphVertex);
          newConstructedVertices.add(editGraphVertex);
          // TODO: you don't want to always draw an edge 
          // TODO: in the case of ngrams in witness and superbase
          // TODO: less edges are needed
          for (EditGraphVertex lastVertex : lastConstructedVertices) {
            INormalizedToken lastToken = lastVertex.getBaseToken();
            int gap = base.isNear(lastToken, match) ?  0 : 1;
            editGraph.add(new EditGraphEdge(lastVertex, editGraphVertex, gap));
          }
        }
        lastConstructedVertices = newConstructedVertices;
      }
    }
    // add edges to end vertex
    EditGraphVertex endVertex = editGraph.getEndVertex();
    for (EditGraphVertex lastVertex : lastConstructedVertices) {
      INormalizedToken lastToken = lastVertex.getBaseToken();
      int gap = base.isNear(lastToken, endVertex.getBaseToken()) ?  0 : 1;
      editGraph.add(new EditGraphEdge(lastVertex, endVertex, gap));
    }
  }
}
