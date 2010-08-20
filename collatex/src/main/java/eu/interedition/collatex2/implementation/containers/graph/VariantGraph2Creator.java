package eu.interedition.collatex2.implementation.containers.graph;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.tokenmatching.TokenIndexMatcher;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraph2Creator {

  private final VariantGraph2 graph;

  public VariantGraph2Creator(VariantGraph2 graph) {
    this.graph = graph;
  }

  // TODO: use the VariantGraphAligner here!
  // write
  // NOTE: tokenA is the token from the Witness
  // For every token in the witness we have to map a VariantNode
  // for matches such a node should already exist
  // however for additions and replacements this will not be the case
  // then we need to add the arcs
  // in some cases the arcs may already exist
  // if they already exist we need to add the witness to the
  // existing arc!
  public void addWitness(IWitness witness) {
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witness);
    makeEdgesForMatches(witness, matches);
  }

  //write
  private void makeEdgesForMatches(IWitness witness, List<ITokenMatch> matches) {
    // Map Tokens in the Graph to Vertices
    Map<INormalizedToken, IVariantGraphVertex> graphTokenToVertex;
    graphTokenToVertex = Maps.newLinkedHashMap();
    for (IVariantGraphVertex vertex: graph.vertexSet()) {
      for (IWitness witness2 : vertex.getWitnesses()) {
        INormalizedToken token = vertex.getToken(witness2);
        graphTokenToVertex.put(token, vertex);
      }
    }
    // Map Tokens in the Witness to the Matches
    Map<INormalizedToken, ITokenMatch> witnessTokenToMatch;
    witnessTokenToMatch = Maps.newLinkedHashMap();
    for (ITokenMatch match : matches) {
      INormalizedToken tokenA = match.getTokenA();
      witnessTokenToMatch.put(tokenA, match);
    }
    addWitnessToGraph(witness, graphTokenToVertex, witnessTokenToMatch);
  }

  private void addWitnessToGraph(IWitness witness, Map<INormalizedToken, IVariantGraphVertex> graphTokenToVertex, Map<INormalizedToken, ITokenMatch> witnessTokenToMatch) {
    IVariantGraphVertex begin = graph.getStartVertex();
    for (INormalizedToken token : witness.getTokens()) {
      if (!witnessTokenToMatch.containsKey(token)) {
        // NOTE: here we determine that the token is an addition/replacement!
        IVariantGraphVertex end = graph.addNewVertex(token, witness);
        graph.addNewEdge(begin, end, witness);
        begin = end;
      } else {
        // NOTE: it is a match!
        ITokenMatch tokenMatch = witnessTokenToMatch.get(token);
        IVariantGraphVertex end = graphTokenToVertex.get(tokenMatch.getTokenB());
        connectBeginToEndVertex(begin, end, witness);
        end.addToken(witness, token);
        begin = end;
      }
    }
    // adds edge from last vertex to end vertex
    IVariantGraphVertex end = graph.getEndVertex();
    connectBeginToEndVertex(begin, end, witness);
  }

  // write
  private void connectBeginToEndVertex(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    if (graph.containsEdge(begin, end)) {
      IVariantGraphEdge existingEdge = graph.getEdge(begin, end);
      existingEdge.addWitness(witness);
    } else {
      graph.addNewEdge(begin, end, witness);
    }
  }

  public static IVariantGraph create(IWitness... witnesses) {
    List<IWitness> witnessList = Lists.newArrayList(witnesses);
    IWitness w1 = witnessList.remove(0);
    IWitness[] w2 = witnessList.toArray(new IWitness[witnessList.size()]);
    VariantGraph2 graph = VariantGraph2.create(w1);
    VariantGraph2Creator creator = new VariantGraph2Creator(graph);
    for (IWitness witness : w2) {
      creator.addWitness(witness);
    }
    return graph;
  }

}
