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

  // Question: do I need to convert this graph to another graph with
  // other weights to determine the shortest path with a standard algo?
  // For now I just remove all the vertices that have a higher value
  // then the minimum amount of sequences for the whole graph
  // This is probably not the end solution
  public List<DGEdge> getShortestPath() {
    Map<DGVertex, Integer> determineMinSequences = determineMinSequences(dGraph);
    int minSequences = determineMinSequences.get(dGraph.getStartVertex());
    DecisionGraph graphWithSinglePath = buildNewGraphWithOnlyMinimumWeightVertices(determineMinSequences, minSequences);
    DGVertex currentVertex = graphWithSinglePath.getStartVertex();
    List<DGEdge> shortestPath = Lists.newArrayList();
    while (graphWithSinglePath.outDegreeOf(currentVertex) == 1) {
      DGEdge edge = graphWithSinglePath.outgoingEdgesOf(currentVertex).iterator().next();
      shortestPath.add(edge);
      currentVertex = edge.getTargetVertex();
    }
    return shortestPath;
  }

  public Map<DGVertex, Integer> determineMinSequences(DecisionGraph graph) {
    DGVertex endVertex = graph.getEndVertex();
    Map<DGVertex, Integer> minSeq = Maps.newLinkedHashMap();
    Map<DGVertex, Integer> gapOrNoGap = Maps.newLinkedHashMap();
    minSeq.put(endVertex, 1); // TODO: CHECK THIS.. !!
    gapOrNoGap.put(endVertex, 0); // insert gap in the end //TODO: check this!!
    // Take topological order of vertices in reserve!
    List<DGVertex> reverse = Lists.reverse(getVerticesInTopologicalOrder(graph));
    Iterator<DGVertex> vertexIterator = reverse.iterator();
    vertexIterator.next(); // NOTE: skip end vertex
    while (vertexIterator.hasNext()) {
      DGVertex vertex = vertexIterator.next();
      Map<DGEdge, Integer> determineMinSequencesProEdge = determineMinSequencesForVertex(graph, minSeq, gapOrNoGap, vertex);
      Integer min = Collections.min(determineMinSequencesProEdge.values());
      // System.out.println("Debugging: "+vertex.getToken().toString()+":"+determineMinSequencesProEdge);
      DGEdge minEdge = findTheMinimumEdge(determineMinSequencesProEdge, min);
      minSeq.put(vertex, min);
      gapOrNoGap.put(vertex, minEdge.getWeight());
    }
    return minSeq;
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
    // System.out.println(vertexToMinWeight);
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

  //TODO: remove this method?
  public DecisionGraph removeChoicesThatIntroduceGaps() {
    int minGaps = determineMinimumNumberOfGaps(dGraph);
    Map<DGVertex, Integer> minWeightForEachVertex = determineMinWeightForEachVertex(dGraph);
    DecisionGraph graph2 = buildNewGraphWithOnlyMinimumWeightVertices(minWeightForEachVertex, minGaps);
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

  private DecisionGraph buildNewGraphWithOnlyMinimumWeightVertices(Map<DGVertex, Integer> minWeightProVertex, int minWeight) {
    Set<DGVertex> verticesToKeep = Sets.newLinkedHashSet();
    for (DGVertex vertex : dGraph.vertexSet()) {
      int weight = minWeightProVertex.get(vertex);
      if (weight <= minWeight) {
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
    DecisionGraph newGraph = new DecisionGraph(dGraph.getStartVertex(), dGraph.getEndVertex());
    for (DGVertex vertex : verticesToKeep) {
      newGraph.addVertex(vertex);
    }
    for (DGEdge edge : newEdges) {
      newGraph.add(edge);
    }
    return newGraph;
  }

  private DGEdge findTheMinimumEdge(Map<DGEdge, Integer> determineMinSequencesProEdge, Integer min) {
    DGEdge result = null;
    for (DGEdge edge : determineMinSequencesProEdge.keySet()) {
      if (determineMinSequencesProEdge.get(edge) == min) {
        result = edge;
        break;
      }
    }
    if (result == null) {
      throw new RuntimeException("This is not supposed to happen!");
    }
    return result;
  }

  private Map<DGEdge, Integer> determineMinSequencesForVertex(DecisionGraph graph, Map<DGVertex, Integer> minSeq, Map<DGVertex, Integer> gapOrNoGap, DGVertex vertex) {
    Set<DGEdge> outgoingEdgesOf = graph.outgoingEdgesOf(vertex);
    if (outgoingEdgesOf.isEmpty()) {
      throw new RuntimeException("Error: "+vertex.toString()+ " has no outgoing edges!");
    }
    Map<DGEdge, Integer> edges = Maps.newLinkedHashMap();
    for (DGEdge outgoing : outgoingEdgesOf) {
      DGVertex targetVertex = outgoing.getTargetVertex();
      // Note: Gaps causes extra sequences.
      // Note: A sequence at the start vertex does not count, cause
      // Note: it will be an empty sequence, cause there are no more tokens
      if (vertex != graph.getStartVertex() && gapOrNoGap.get(targetVertex) == 0 && outgoing.getWeight() == 1) {
        edges.put(outgoing, 1 + minSeq.get(targetVertex));
      } else {
        edges.put(outgoing, minSeq.get(targetVertex));
      }
    }
    return edges;
  }

}
