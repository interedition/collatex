package eu.interedition.collatex2.implementation.decision_graph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DecisionGraphVisitor {

  private final DecisionGraph dGraph;

  public DecisionGraphVisitor(DecisionGraph dGraph) {
    this.dGraph = dGraph;
  }

  // TODO: remove static!
  public static Map<DGVertex, Integer> determineMinWeightForEachVertex(DecisionGraph graph) {
    // bepalen minimaal aantal gaps in de decision graph
    Map<DGVertex, Integer> vertexToMinWeight = Maps.newLinkedHashMap();
    Iterator<DGVertex> iterator = graph.iterator();
    // setup map with startvertex
    DGVertex startVertex = iterator.next();
    vertexToMinWeight.put(startVertex, 0);
    while (iterator.hasNext()) {
      DGVertex next = iterator.next();
      Set<DGEdge> incomingEdgesOf = graph.incomingEdgesOf(next);
      // bepaal de nieuwe weight for elk van de incoming edges
      Map<DGEdge, Integer> edgeToTotalWeight = Maps.newLinkedHashMap();
      for (DGEdge incomingEdge : incomingEdgesOf) {
        edgeToTotalWeight.put(incomingEdge, vertexToMinWeight.get(incomingEdge.getBeginVertex()) + incomingEdge.getWeight());
      }
      Integer min = Collections.min(edgeToTotalWeight.values());
      vertexToMinWeight.put(next, min);
    }
    System.out.println(vertexToMinWeight);
    return vertexToMinWeight;
  }

  // TODO: remove static!
  public static int determineMinimumNumberOfGaps(DecisionGraph dGraph) {
    // we moeten de weight van de end vertex hebben om de rest te filteren..
    Map<DGVertex, Integer> vertexToMinWeight = determineMinWeightForEachVertex(dGraph);
    DGVertex endVertex = dGraph.getEndVertex();
    int minGaps = vertexToMinWeight.get(endVertex);
    // System.out.println("Mininum number of gaps in the alignment: "+minGaps);
    return minGaps;
  }

  // TODO: implement!
  public List<DGEdge> getShortestPath() {
    // TODO Auto-generated method stub
    return null;
  }

  public DecisionGraph removeChoicesThatIntroduceGaps() {
    int minGaps = determineMinimumNumberOfGaps(dGraph);
    Map<DGVertex, Integer> minWeightForEachVertex = determineMinWeightForEachVertex(dGraph);
    Set<DGVertex> verticesToKeep = Sets.newLinkedHashSet();
    for (DGVertex vertex : dGraph.vertexSet()) {
      int weight = minWeightForEachVertex.get(vertex);
      if (weight <= minGaps) {
        verticesToKeep.add(vertex);
      }
    }
    Set<DGEdge> edgeSet = dGraph.edgeSet();
    Set<DGEdge> newEdges = Sets.newLinkedHashSet();
    for (DGEdge edge : edgeSet) {
      if (verticesToKeep.contains(edge.getBeginVertex()) && verticesToKeep.contains(edge.getTargetVertex())) {
        DGEdge newEdge = new DGEdge(edge.getBeginVertex(), edge.getTargetVertex(), -1);
        newEdges.add(newEdge);
      }
    }

    DecisionGraph graph2 = new DecisionGraph(dGraph.getStartVertex(), dGraph.getEndVertex());
    verticesToKeep.remove(dGraph.getStartVertex());
    verticesToKeep.remove(dGraph.getEndVertex());
    for (DGVertex vertex : verticesToKeep) {
      graph2.addVertex(vertex);
    }
    for (DGEdge edge : newEdges) {
      graph2.add(edge);
    }
    return graph2;
  }

}
