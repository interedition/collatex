package eu.interedition.collatex2.implementation.decision_graph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DecisionGraphVisitor {

  private final DecisionGraph dGraph;

  public DecisionGraphVisitor(DecisionGraph dGraph) {
    this.dGraph = dGraph;
  }

  // TODO: implement!
  public List<DGEdge> getShortestPath() {
    // TODO Auto-generated method stub
    return null;
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
        DGEdge newEdge = new DGEdge(edge.getBeginVertex(), edge.getTargetVertex(), edge.getWeight());
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

  private List<DGVertex> getVerticesInTopologicalOrder(DecisionGraph dGraph2) {
    List<DGVertex> topoVertices = Lists.newArrayList();
    Iterator<DGVertex> topologicalOrder = dGraph2.iterator();
    while (topologicalOrder.hasNext()) {
      topoVertices.add(topologicalOrder.next());
    }
    return topoVertices;
  }

  //NOTE: very experimental!
  //NOTE: this method is a real simple implementation
  //NOTE: not correct in all cases!
  public Map<DGVertex, Integer> assignLCSWeight(DecisionGraph dGraph2) {
    DGVertex endVertex = dGraph2.getEndVertex();
    Map<DGVertex, Integer> maxLCS = Maps.newLinkedHashMap();
    maxLCS.put(endVertex, 0);
    // misschien moet ik hier gewoon de topological order nemen in reverse!
    List<DGVertex> reverse = Lists.reverse(getVerticesInTopologicalOrder(dGraph2));
    Iterator<DGVertex> vertexIterator = reverse.iterator();
    vertexIterator.next(); // NOTE: skip end vertex
    DGVertex vertex;
    //TODO: Use vertex iterator here! In that case I would lose
    //TODO: the do while construction!
    do {
      vertex = vertexIterator.next();
      System.out.println("#"+vertex);
      Map<DGEdge, Integer> determineMaxLCSForVertex = determineMaxLCSForVertex(dGraph2, maxLCS, vertex);
      System.out.println(determineMaxLCSForVertex.size());
      Integer max = Collections.max(determineMaxLCSForVertex.values());
      maxLCS.put(vertex, max);
    } while (vertex != dGraph2.getStartVertex()); 
    System.out.println("Max LCS"+maxLCS);
    return maxLCS;
  }

  private Map<DGEdge, Integer> determineMaxLCSForVertex(DecisionGraph dGraph2, Map<DGVertex, Integer> maxLCS, DGVertex vertex) {
    Set<DGEdge> outgoingEdges = dGraph2.outgoingEdgesOf(vertex);
    // nu proberen we het maximum LCS te berekenen van al de OUTgoing edges
    Map<DGEdge, Integer> lcs = Maps.newLinkedHashMap();
    for (DGEdge outgoing: outgoingEdges) {
      System.out.println("!"+outgoing.getWeight());
      DGVertex targetVertex = outgoing.getTargetVertex();
      Integer maxLCSParent = maxLCS.get(targetVertex);
      if (outgoing.getWeight()==0) {
        lcs.put(outgoing, maxLCSParent+1);
      } else {
        //NOTE: I can return here either 0..
        //NOTE: Then I would have to traverse specifically through parts of the graph
        //NOTE: where there is doubt of which route to take 
        //NOTE: or I return the MaxLCS encountered so far
        //NOTE: which leads to an artificially high value
        lcs.put(outgoing, maxLCSParent);
      }
    }
    return lcs;
  }
  
  public Map<DGVertex, Integer> determineMinSequences(DecisionGraph graph) {
    DGVertex endVertex = graph.getEndVertex();
    Map<DGVertex, Integer> minSeq = Maps.newLinkedHashMap();
    Map<DGVertex, Integer> gapOrNoGap = Maps.newLinkedHashMap();
    minSeq.put(endVertex, 1);
    gapOrNoGap.put(endVertex, 0); // insert gap in the end 
    // Take topological order of vertices in reserve!
    List<DGVertex> reverse = Lists.reverse(getVerticesInTopologicalOrder(graph));
    Iterator<DGVertex> vertexIterator = reverse.iterator();
    vertexIterator.next(); // NOTE: skip end vertex
    while(vertexIterator.hasNext()) {
      DGVertex vertex = vertexIterator.next();
      Set<DGEdge> outgoingEdgesOf = graph.outgoingEdgesOf(vertex);
      //TODO: which ones of the outgoing edges should we chose when there are 
      //multiple ones? Ik zou de edge met de minimum maxLCS nemen?
      for (DGEdge outgoing : outgoingEdgesOf) {
        DGVertex targetVertex = outgoing.getTargetVertex();
        if(targetVertex.getToken().getNormalized().equals("the")) {
          System.out.println("DEBUG "+gapOrNoGap.get(targetVertex)+":"+outgoing.getWeight());
        }
        if (gapOrNoGap.get(targetVertex)==0&&outgoing.getWeight()==1) {
          minSeq.put(vertex, 1+minSeq.get(targetVertex));
        } else {
          minSeq.put(vertex, minSeq.get(targetVertex));
        }
        gapOrNoGap.put(vertex, outgoing.getWeight());
      }
    }
    return minSeq;
  }

}
