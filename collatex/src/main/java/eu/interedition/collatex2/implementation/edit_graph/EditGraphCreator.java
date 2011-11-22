package eu.interedition.collatex2.implementation.edit_graph;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex2.implementation.matching.TokenMatcher;
import eu.interedition.collatex2.implementation.vg_alignment.EndToken;
import eu.interedition.collatex2.implementation.vg_alignment.StartToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class EditGraphCreator {
  private final EditGraph editGraph;

  public EditGraphCreator() {
    this(new EditGraph());
  }

  public EditGraphCreator(EditGraph editGraph) {
    this.editGraph = editGraph;
  }

  public EditGraph buildEditGraph(IWitness a, IWitness b) {
    // create start vertex
    EditGraphVertex startVertex = new EditGraphVertex(null, new StartToken());
    editGraph.setStartVertex(startVertex);
    Set<EditGraphVertex> lastConstructedVertices = Sets.newLinkedHashSet();
    lastConstructedVertices.add(startVertex);
    // build the decision graph from the matches and the variant graph
    TokenMatcher matcher = new EqualityTokenComparator();
    Multimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    // add for vertices for witness tokens that have a matching base token
    for (INormalizedToken wToken : b.getTokens()) {
      Collection<INormalizedToken> matchingTokens = matches.get(wToken);
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
            EditOperation operation = a.isNear(lastToken, match) ?  EditOperation.NO_GAP : EditOperation.GAP;
            editGraph.add(new EditGraphEdge(lastVertex, editGraphVertex, operation));
          }
        }
        lastConstructedVertices = newConstructedVertices;
      }
    }
    // create end vertex
    EditGraphVertex endVertex = new EditGraphVertex(null, new EndToken(a.size()+1));
    editGraph.setEndVertex(endVertex);
    // add edges to end vertex
    //TODO: remove duplication!
    for (EditGraphVertex lastVertex : lastConstructedVertices) {
      INormalizedToken lastToken = lastVertex.getBaseToken();
      EditOperation operation = a.isNear(lastToken, endVertex.getBaseToken()) ?  EditOperation.NO_GAP : EditOperation.GAP;
      editGraph.add(new EditGraphEdge(lastVertex, endVertex, operation));
    }
    return editGraph;
  }
}
