package eu.interedition.collatex2.implementation.decision_graph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.ILinker;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class NewLinker implements ILinker {
  
  public Map<INormalizedToken, INormalizedToken> link(IVariantGraph vGraph, IWitness b) {
    VariantGraphMatcher vgmatcher = new VariantGraphMatcher();
    DecisionGraphCreator creator2 = new DecisionGraphCreator(vgmatcher, vGraph, b);
    DecisionGraph dGraph = creator2.buildDecisionGraph();
    DecisionGraphVisitor visitor = new DecisionGraphVisitor(dGraph);
    List<DGEdge> shortestPath = visitor.getShortestPath();
    Iterator<DGEdge> edges = shortestPath.iterator();
//    for (DGEdge edge : shortestPath) {
//      System.out.println(edge.getTargetVertex().toString());
//    }
    //Note: This is the second time the matcher function is called
    ListMultimap<INormalizedToken, INormalizedToken> matches = vgmatcher.match(vGraph, b);
    Map<INormalizedToken, INormalizedToken> linkedTokens = Maps.newLinkedHashMap();
    List<INormalizedToken> tokens = b.getTokens();
    for (INormalizedToken token : tokens) {
      if (matches.containsKey(token)) {
        DGEdge edge = edges.next();
        linkedTokens.put(token, edge.getTargetVertex().getToken());
      }
    }
    return linkedTokens;
  }
}
