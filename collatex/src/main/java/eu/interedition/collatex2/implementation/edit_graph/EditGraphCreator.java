package eu.interedition.collatex2.implementation.edit_graph;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.matching.TokenMatcher;
import eu.interedition.collatex2.implementation.matching.VariantGraphMatcher;
import eu.interedition.collatex2.implementation.vg_alignment.EndToken;
import eu.interedition.collatex2.implementation.vg_alignment.StartToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class EditGraphCreator {
  private final EditGraph editGraph;

  public EditGraphCreator() {
    this(new EditGraph());
  }

  public EditGraphCreator(EditGraph editGraph) {
    this.editGraph = editGraph;
  }

  //TODO: remove this method in favor of the second one!
  public EditGraph buildEditGraph(VariantGraphMatcher matcher, IVariantGraph vGraph, IWitness b) {
    // create start vertex
    //TODO: that eight there is not handy!
    //TODO: the end vertex is unique by itself...
    //TODO: override the equals!
    editGraph.setEndVertex(new EditGraphVertex(null, new EndToken(8)));
    editGraph.setStartVertex(new EditGraphVertex(null, vGraph.getStartVertex()));
    

    Set<EditGraphVertex> lastConstructedVertices = Sets.newLinkedHashSet();
    lastConstructedVertices.add(editGraph.getStartVertex());
    // build the decision graph from the matches and the variant graph
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(vGraph, b);
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
  public EditGraph buildEditGraph(IWitness a, IWitness b2) {
    // create start vertex
    EditGraphVertex startVertex = new EditGraphVertex(null, new StartToken());
    editGraph.setStartVertex(startVertex);
    Set<EditGraphVertex> lastConstructedVertices = Sets.newLinkedHashSet();
    lastConstructedVertices.add(startVertex);
    // build the decision graph from the matches and the variant graph
    TokenMatcher matcher = new TokenMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b2);
       // add for vertices for witness tokens that have a matching base token
    for (INormalizedToken wToken : b2.getTokens()) {
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
            int gap = a.isNear(lastToken, match) ?  0 : 1;
            editGraph.add(new EditGraphEdge(lastVertex, editGraphVertex, gap));
          }
        }
        lastConstructedVertices = newConstructedVertices;
      }
    }
    // create end vertex
    EditGraphVertex endVertex = new EditGraphVertex(null, new EndToken(a.size()+1));
    editGraph.setEndVertex(endVertex);
    // add edges to end vertex
    for (EditGraphVertex lastVertex : lastConstructedVertices) {
      INormalizedToken lastToken = lastVertex.getBaseToken();
      int gap = a.isNear(lastToken, endVertex.getBaseToken()) ?  0 : 1;
      editGraph.add(new EditGraphEdge(lastVertex, endVertex, gap));
    }
    return editGraph;
  }
}
