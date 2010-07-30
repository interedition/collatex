package eu.interedition.collatex2.experimental.graph;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.graph.indexing.IVariantGraphIndex;
import eu.interedition.collatex2.experimental.graph.indexing.VariantGraphIndexMatcher;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraph2Creator {

  private final VariantGraph2 graph;

  public VariantGraph2Creator(VariantGraph2 graph) {
    this.graph = graph;
  }

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
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witness);
    makeEdgesForMatches(witness, matches, matcher.getGraphIndex());
  }

  //write
  private void makeEdgesForMatches(IWitness witness, List<ITokenMatch> matches, IVariantGraphIndex graphIndex2) {
    Map<INormalizedToken, ITokenMatch> witnessTokenToMatch;
    witnessTokenToMatch = Maps.newLinkedHashMap();
    for (ITokenMatch match : matches) {
      INormalizedToken tokenA = match.getTokenA();
      witnessTokenToMatch.put(tokenA, match);
    }
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
        IVariantGraphVertex end = graphIndex2.getVertex(tokenMatch.getTokenB());
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
