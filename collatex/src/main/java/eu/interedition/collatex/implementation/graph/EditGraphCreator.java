package eu.interedition.collatex.implementation.graph;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.matching.Matches;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class EditGraphCreator {
  Logger LOG = LoggerFactory.getLogger(EditGraphCreator.class);
  private final EditGraph editGraph;

  public EditGraphCreator() {
    this(new EditGraph());
  }

  public EditGraphCreator(EditGraph editGraph) {
    this.editGraph = editGraph;
  }

  public EditGraph buildEditGraph(IWitness a, IWitness b, Comparator<Token> comparator) {
    // create start vertex
    EditGraphVertex startVertex = new EditGraphVertex(null, SimpleToken.START);
    editGraph.setStartVertex(startVertex);
    Set<EditGraphVertex> lastConstructedVertices = Sets.newLinkedHashSet();
    lastConstructedVertices.add(startVertex);
    // build the decision graph from the matches and the variant graph
    Matches m = Matches.between(a, b, comparator);
    Set<String> ambiguousNormalized = getAmbiguousNormalizedContent(m);
    Multimap<Token, Token> matches = m.getAll();
    // add for vertices for witness tokens that have a matching base token
    int witnessIndex = 0;
    int lastMatchIndex = -1;
    for (Token wToken : b.getTokens()) {
      Collection<Token> matchingTokens = matches.get(wToken);
      if (!matchingTokens.isEmpty()) {
        Set<EditGraphVertex> newConstructedVertices = Sets.newLinkedHashSet();
        for (Token match : matchingTokens) {
          EditGraphVertex editGraphVertex = new EditGraphVertex(wToken, match);
          editGraph.add(editGraphVertex);
          newConstructedVertices.add(editGraphVertex);

          // TODO: you don't want to always draw an edge 
          // TODO: in the case of ngrams in witness and superbase
          // TODO: less edges are needed
          for (EditGraphVertex lastVertex : lastConstructedVertices) {
            Token lastToken = lastVertex.getBaseToken();
            int score = witnessIndex - lastMatchIndex - 1;
            EditOperation operation;
            if (a.isNear(lastToken, match)) {
              operation = EditOperation.NO_GAP;
            } else {
              operation = EditOperation.GAP;
              score++;
            }
            editGraph.add(new EditGraphEdge(lastVertex, editGraphVertex, operation, score));
          }
        }
        lastConstructedVertices = newConstructedVertices;
        lastMatchIndex = witnessIndex;
      }
      witnessIndex++;
    }
    // create end vertex
    EditGraphVertex endVertex = new EditGraphVertex(null, SimpleToken.END);
    editGraph.setEndVertex(endVertex);
    // add edges to end vertex
    //TODO: remove duplication!
    for (EditGraphVertex lastVertex : lastConstructedVertices) {
      Token lastToken = lastVertex.getBaseToken();
      EditGraphEdge edge = a.isNear(lastToken, endVertex.getBaseToken()) ? new EditGraphEdge(lastVertex, endVertex, EditOperation.NO_GAP, 0) : new EditGraphEdge(lastVertex, endVertex, EditOperation.GAP, 1);
      editGraph.add(edge);
    }

    //    addSkipVertices(ambiguousNormalized);

    return editGraph;
  }

  private void addSkipVertices(Set<String> ambiguousNormalized) {
    Set<EditGraphVertex> vertices = Sets.newLinkedHashSet();
    Set<EditGraphEdge> edges = Sets.newLinkedHashSet();
    Iterator<EditGraphVertex> iterator = editGraph.iterator();
    while (iterator.hasNext()) {
      EditGraphVertex vertex = iterator.next();
      Token witnessToken = vertex.getWitnessToken();
      if (witnessToken != null) {
        String normalized = ((SimpleToken) witnessToken).getNormalized();
        if (ambiguousNormalized.contains(normalized)) {
          Set<EditGraphEdge> incomingEdges = editGraph.incomingEdgesOf(vertex);
          Set<EditGraphEdge> outgoingEdges = editGraph.outgoingEdgesOf(vertex);
          //        for (EditGraphEdge incomingEdge : incomingEdges) {
          //        for (EditGraphEdge outgoingEdge : outgoingEdges) {
          //          int score = 3;
          //          edges.add(new EditGraphEdge(incomingEdge.getSourceVertex(), outgoingEdge.getTargetVertex(), EditOperation.GAP, score));
          //        }
          //      }
          EditGraphVertex skipVertex = null; // FIXME: new EditGraphVertex(new NormalizedToken(null, 0, "", ""), new VariantGraphVertex("", new NormalizedToken(null, 0, "", "")));
          vertices.add(skipVertex);
          for (EditGraphEdge incomingEdge : incomingEdges) {
            int score = 3;
            edges.add(new EditGraphEdge(incomingEdge.getSourceVertex(), skipVertex, EditOperation.GAP, score));
          }
          for (EditGraphEdge outgoingEdge : outgoingEdges) {
            int score = 0;
            edges.add(new EditGraphEdge(skipVertex, outgoingEdge.getTargetVertex(), EditOperation.NO_GAP, score));
          }
        }
      }
    }
    for (EditGraphVertex editGraphVertex : vertices) {
      editGraph.add(editGraphVertex);
    }
    for (EditGraphEdge editGraphEdge : edges) {
      editGraph.add(editGraphEdge);
    }
  }

  private Set<String> getAmbiguousNormalizedContent(Matches m) {
    Set<Token> ambiguousMatches = m.getAmbiguous();
    Set<String> ambiguousNormalized = Sets.newHashSet();
    for (Token token : ambiguousMatches) {
      ambiguousNormalized.add(((SimpleToken) token).getNormalized());
    }
    return ambiguousNormalized;
  }
}
