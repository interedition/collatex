package eu.interedition.collatex2.implementation.graph.edit;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EditGraphVisitor {

  private final EditGraph editGraph;

  public EditGraphVisitor(EditGraph editGraph) {
    this.editGraph = editGraph;
  }

  // Question: do I need to convert this graph to another graph with
  // other weights to determine the shortest path with a standard algo?
  // For now I just remove all the vertices that have a higher value
  // then the minimum amount of sequences for the whole graph
  // This is probably not the end solution
  public List<EditGraphEdge> getShortestPath() {
    Map<EditGraphVertex, Integer> determineMinSequences = determineMinSequences(editGraph);
    int minSequences = determineMinSequences.get(editGraph.getStartVertex());
    EditGraph graphWithSinglePath = buildNewGraphWithOnlyMinimumWeightVertices(determineMinSequences, minSequences);
    EditGraphVertex currentVertex = graphWithSinglePath.getStartVertex();
    List<EditGraphEdge> shortestPath = Lists.newArrayList();
    while (graphWithSinglePath.outDegreeOf(currentVertex) == 1) {
      EditGraphEdge edge = graphWithSinglePath.outgoingEdgesOf(currentVertex).iterator().next();
      shortestPath.add(edge);
      currentVertex = edge.getTargetVertex();
    }
    if(graphWithSinglePath.outDegreeOf(currentVertex) > 1) {
        throw new RuntimeException("Vertex "+currentVertex+" has more than one possi!");
    }
    return shortestPath;
  }

  public Map<EditGraphVertex, Integer> determineMinSequences(EditGraph graph) {
    EditGraphVertex endVertex = graph.getEndVertex();
    Map<EditGraphVertex, Integer> minSeq = Maps.newLinkedHashMap();
    Map<EditGraphVertex, Integer> gapOrNoGap = Maps.newLinkedHashMap();
    minSeq.put(endVertex, 1); // TODO: CHECK THIS.. !!
    gapOrNoGap.put(endVertex, 0); // insert gap in the end //TODO: check this!!
    // Take topological order of vertices in reserve!
    List<EditGraphVertex> reverse = Lists.reverse(getVerticesInTopologicalOrder(graph));
    Iterator<EditGraphVertex> vertexIterator = reverse.iterator();
    vertexIterator.next(); // NOTE: skip end vertex
    while (vertexIterator.hasNext()) {
      EditGraphVertex vertex = vertexIterator.next();
      Map<EditGraphEdge, Integer> determineMinSequencesProEdge = determineMinSequencesForVertex(graph, minSeq, gapOrNoGap, vertex);
      Integer min = Collections.min(determineMinSequencesProEdge.values());
      // System.out.println("Debugging: "+vertex.getToken().toString()+":"+determineMinSequencesProEdge);
      EditGraphEdge minEdge = findTheMinimumEdge(determineMinSequencesProEdge, min);
      minSeq.put(vertex, min);
      gapOrNoGap.put(vertex, minEdge.getScore());
    }
    return minSeq;
  }


  // TODO: remove static!
  public static Map<EditGraphVertex, Integer> determineMinWeightForEachVertex(EditGraph graph) {
    // bepalen minimaal aantal gaps in de decision graph
    Map<EditGraphVertex, Integer> vertexToMinWeight = Maps.newLinkedHashMap();
    Iterator<EditGraphVertex> iterator = graph.iterator();
    // setup map with startvertex
    EditGraphVertex startVertex = iterator.next();
    vertexToMinWeight.put(startVertex, 0);
    while (iterator.hasNext()) {
      EditGraphVertex next = iterator.next();
      Set<EditGraphEdge> incomingEdgesOf = graph.incomingEdgesOf(next);
      // bepaal de nieuwe weight for elk van de incoming edges
      Map<EditGraphEdge, Integer> edgeToTotalWeight = Maps.newLinkedHashMap();
      for (EditGraphEdge incomingEdge : incomingEdgesOf) {
        edgeToTotalWeight.put(incomingEdge, vertexToMinWeight.get(incomingEdge.getSourceVertex()) + incomingEdge.getScore());
      }
      Integer min = Collections.min(edgeToTotalWeight.values());
      vertexToMinWeight.put(next, min);
    }
    // System.out.println(vertexToMinWeight);
    return vertexToMinWeight;
  }

  // TODO: remove static!
  public static int determineMinimumNumberOfGaps(EditGraph dGraph) {
    // we moeten de weight van de end vertex hebben om de rest te filteren..
    Map<EditGraphVertex, Integer> vertexToMinWeight = determineMinWeightForEachVertex(dGraph);
    EditGraphVertex endVertex = dGraph.getEndVertex();
    int minGaps = vertexToMinWeight.get(endVertex);
    // System.out.println("Mininum number of gaps in the alignment: "+minGaps);
    return minGaps;
  }

  //TODO: remove this method?
  public EditGraph removeChoicesThatIntroduceGaps() {
    int minGaps = determineMinimumNumberOfGaps(editGraph);
    Map<EditGraphVertex, Integer> minWeightForEachVertex = determineMinWeightForEachVertex(editGraph);
    EditGraph graph2 = buildNewGraphWithOnlyMinimumWeightVertices(minWeightForEachVertex, minGaps);
    return graph2;
  }

  private List<EditGraphVertex> getVerticesInTopologicalOrder(EditGraph dGraph2) {
    List<EditGraphVertex> topoVertices = Lists.newArrayList();
    Iterator<EditGraphVertex> topologicalOrder = dGraph2.iterator();
    while (topologicalOrder.hasNext()) {
      topoVertices.add(topologicalOrder.next());
    }
    return topoVertices;
  }

  private EditGraph buildNewGraphWithOnlyMinimumWeightVertices(Map<EditGraphVertex, Integer> minWeightProVertex, int minWeight) {
    Set<EditGraphVertex> verticesToKeep = Sets.newLinkedHashSet();
    for (EditGraphVertex vertex : editGraph.vertexSet()) {
      int weight = minWeightProVertex.get(vertex);
      if (weight <= minWeight) {
        verticesToKeep.add(vertex);
      }
    }
    Set<EditGraphEdge> edgeSet = editGraph.edgeSet();
    Set<EditGraphEdge> newEdges = Sets.newLinkedHashSet();
    for (EditGraphEdge edge : edgeSet) {
      if (verticesToKeep.contains(edge.getSourceVertex()) && verticesToKeep.contains(edge.getTargetVertex())) {
        EditGraphEdge newEdge = new EditGraphEdge(edge.getSourceVertex(), edge.getTargetVertex(), edge.getEditOperation(), edge.getScore());
        newEdges.add(newEdge);
      }
    }
    EditGraph newGraph = new EditGraph();
    newGraph.setStartVertex(editGraph.getStartVertex());
    newGraph.setEndVertex(editGraph.getEndVertex());
    for (EditGraphVertex vertex : verticesToKeep) {
      newGraph.addVertex(vertex);
    }
    for (EditGraphEdge edge : newEdges) {
      newGraph.add(edge);
    }
    return newGraph;
  }

  private EditGraphEdge findTheMinimumEdge(Map<EditGraphEdge, Integer> determineMinSequencesProEdge, Integer min) {
    EditGraphEdge result = null;
    for (EditGraphEdge edge : determineMinSequencesProEdge.keySet()) {
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

  private Map<EditGraphEdge, Integer> determineMinSequencesForVertex(EditGraph graph, Map<EditGraphVertex, Integer> minSeq, Map<EditGraphVertex, Integer> gapOrNoGap, EditGraphVertex vertex) {
    Set<EditGraphEdge> outgoingEdgesOf = graph.outgoingEdgesOf(vertex);
    if (outgoingEdgesOf.isEmpty()) {
      throw new RuntimeException("Error: "+vertex.toString()+ " has no outgoing edges!");
    }
    Map<EditGraphEdge, Integer> edges = Maps.newLinkedHashMap();
    for (EditGraphEdge outgoing : outgoingEdgesOf) {
      EditGraphVertex targetVertex = outgoing.getTargetVertex();
      // Note: Gaps causes extra sequences.
      // Note: A sequence at the start vertex does not count, cause
      // Note: it will be an empty sequence, cause there are no more tokens
      if (vertex != graph.getStartVertex() && gapOrNoGap.get(targetVertex) == 0 && outgoing.getEditOperation() == EditOperation.GAP) {
        edges.put(outgoing, 1 + minSeq.get(targetVertex));
      } else {
        edges.put(outgoing, minSeq.get(targetVertex));
      }
    }
    return edges;
  }

}
