package eu.interedition.collatex2.implementation.decision_graph;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class NewLinker {
  
  public Map<INormalizedToken, INormalizedToken> link(IVariantGraph vGraph, IWitness b) {
    VariantGraphMatcher vgmatcher = new VariantGraphMatcher();
    DecisionGraphCreator creator2 = new DecisionGraphCreator(vgmatcher, vGraph, b);
    DecisionGraph dGraph = creator2.buildDecisionGraph();
    DecisionGraphVisitor visitor = new DecisionGraphVisitor(dGraph);
    List<DGEdge> shortestPath = visitor.getShortestPath();
//    for (DGEdge edge : shortestPath) {
//      System.out.println(edge.getTargetVertex().toString());
//    }
    //Ik moet hier de matches hebben!
    Map<INormalizedToken, INormalizedToken> linkedTokens = Maps.newLinkedHashMap();
    return linkedTokens;
  }

}
