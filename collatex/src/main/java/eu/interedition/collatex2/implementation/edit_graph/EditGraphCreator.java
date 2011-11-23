package eu.interedition.collatex2.implementation.edit_graph;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import eu.interedition.collatex2.implementation.containers.witness.WitnessToken;
import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex2.implementation.matching.Matches;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

import java.util.Collection;
import java.util.Set;

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
    EditGraphVertex startVertex = new EditGraphVertex(null, WitnessToken.START);
    editGraph.setStartVertex(startVertex);
    Set<EditGraphVertex> lastConstructedVertices = Sets.newLinkedHashSet();
    lastConstructedVertices.add(startVertex);
    // build the decision graph from the matches and the variant graph
    Multimap<INormalizedToken, INormalizedToken> matches = Matches.between(a, b, new EqualityTokenComparator()).getAll();
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
            EditGraphEdge edge = a.isNear(lastToken, match) ?  new EditGraphEdge(lastVertex, editGraphVertex, EditOperation.NO_GAP, 0) : new EditGraphEdge(lastVertex, editGraphVertex, EditOperation.GAP, 1);
            editGraph.add(edge);
          }
        }
        lastConstructedVertices = newConstructedVertices;
      }
    }
    // create end vertex
    EditGraphVertex endVertex = new EditGraphVertex(null, WitnessToken.END);
    editGraph.setEndVertex(endVertex);
    // add edges to end vertex
    //TODO: remove duplication!
    for (EditGraphVertex lastVertex : lastConstructedVertices) {
      INormalizedToken lastToken = lastVertex.getBaseToken();
      EditGraphEdge edge = a.isNear(lastToken, endVertex.getBaseToken()) ?  new EditGraphEdge(lastVertex, endVertex, EditOperation.NO_GAP, 0) : new EditGraphEdge(lastVertex, endVertex, EditOperation.GAP, 1);
      editGraph.add(edge);
    }
    return editGraph;
  }
}
