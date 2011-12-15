package eu.interedition.collatex.implementation.graph;

import java.util.*;

import com.google.common.collect.*;

import eu.interedition.collatex.implementation.matching.Matches;
import eu.interedition.collatex.implementation.output.DotExporter;
import eu.interedition.collatex.interfaces.Token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditGraphVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(EditGraphVisitor.class);
  private final EditGraph editGraph;

  public EditGraphVisitor(EditGraph editGraph) {
    this.editGraph = editGraph;
  }

  // Question: do I need to convert this graph to another graph with
  // other weights to determine the shortest path with a standard algo?
  // For now I just remove all the vertices that have a higher value
  // than the minimum amount of sequences for the whole graph
  // This is probably not the end solution
  public List<EditGraphEdge> getShortestPath(Matches match) {
    exportGraphBefore();

    Map<EditGraphVertex, Integer> determineMinSequences = determineMinSequences(editGraph);
    int minSequences = determineMinSequences.get(editGraph.getStart());

    //    EditGraph graphWithSinglePath = buildNewGraphWithOnlyMinimumWeightVertices(determineMinSequences, match);
    EditGraph graphWithSinglePath = buildNewGraphWithOnlyMinimumScoreEdges(match);

    EditGraphVertex currentVertex = graphWithSinglePath.getStart();
    List<EditGraphEdge> shortestPath = Lists.newArrayList();
    while (Iterables.size(currentVertex.outgoing()) == 1) {
      EditGraphEdge edge = Iterables.getFirst(currentVertex.outgoing(), null);
      shortestPath.add(edge);
      currentVertex = edge.to();
    }

    exportGraphAfter(graphWithSinglePath);

    if (Iterables.size(currentVertex.outgoing()) > 1) {
      throw new RuntimeException("Vertex " + currentVertex + " has more than one possible outgoing edge!");
    }
    return shortestPath;
  }

  private void exportGraphBefore() {
    LOG.info("generating shortestpath_before.svg");
    DotExporter.generateSVG("site/collation/shortestpath_before.svg", DotExporter.toDot(editGraph), "Shortest Path before");
  }

  private void exportGraphAfter(EditGraph graphWithSinglePath) {
    LOG.info("generating shortestpath_after.svg");
    DotExporter.generateSVG("site/collation/shortestpath_after.svg", DotExporter.toDot(graphWithSinglePath), "Shortest Path after");
  }

  public Map<EditGraphVertex, Integer> determineMinSequences(EditGraph graph) {
    EditGraphVertex endVertex = graph.getEnd();
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
    Iterator<EditGraphVertex> iterator = graph.vertices().iterator();
    // setup map with startvertex
    EditGraphVertex startVertex = iterator.next();
    vertexToMinWeight.put(startVertex, 0);
    while (iterator.hasNext()) {
      EditGraphVertex next = iterator.next();
      Set<EditGraphEdge> incomingEdgesOf = Sets.newHashSet(next.incoming());
      // bepaal de nieuwe weight for elk van de incoming edges
      Map<EditGraphEdge, Integer> edgeToTotalWeight = Maps.newLinkedHashMap();
      for (EditGraphEdge incomingEdge : incomingEdgesOf) {
        edgeToTotalWeight.put(incomingEdge, vertexToMinWeight.get(incomingEdge.from()) + incomingEdge.getScore());
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
    EditGraphVertex endVertex = dGraph.getEnd();
    int minGaps = vertexToMinWeight.get(endVertex);
    // System.out.println("Mininum number of gaps in the alignment: "+minGaps);
    return minGaps;
  }

  //TODO: remove this method?
  public EditGraph removeChoicesThatIntroduceGaps(Matches matches) {
    int minGaps = determineMinimumNumberOfGaps(editGraph);
    Map<EditGraphVertex, Integer> minWeightForEachVertex = determineMinWeightForEachVertex(editGraph);
    EditGraph graph2 = buildNewGraphWithOnlyMinimumWeightVertices(minWeightForEachVertex, matches);
    return graph2;
  }

  private List<EditGraphVertex> getVerticesInTopologicalOrder(EditGraph dGraph2) {
    List<EditGraphVertex> topoVertices = Lists.newArrayList();
    Iterator<EditGraphVertex> topologicalOrder = dGraph2.vertices().iterator();
    while (topologicalOrder.hasNext()) {
      topoVertices.add(topologicalOrder.next());
    }
    return topoVertices;
  }

  private EditGraph buildNewGraphWithOnlyMinimumScoreEdges(Matches matches) {
    Set<EditGraphEdge> minimumScoreEdges = getMinimumScoreEdges(matches);
    Set<EditGraphVertex> vertices = getVertices(minimumScoreEdges);
    EditGraph newEditGraph = newEditGraph(minimumScoreEdges, vertices);

    List<EditGraphVertex> verticesToRemove = Lists.newArrayList();
    List<EditGraphEdge> edgesToRemove = Lists.newArrayList();
    List<EditGraphEdge> edgesToAdd = Lists.newArrayList();
    Iterator<EditGraphVertex> iterator = newEditGraph.vertices().iterator();
    while (iterator.hasNext()) {
      EditGraphVertex editGraphVertex = iterator.next();
      Token witnessToken = editGraphVertex.getWitness();
      LOG.info("witnessToken={}", witnessToken);
      if (witnessToken != null && witnessToken.getContent().equals("")) {
        LOG.info("skipVertex!");
        // skipvertex, remove
        verticesToRemove.add(editGraphVertex);
        Set<EditGraphEdge> incomingEdges = Sets.newHashSet(editGraphVertex.incoming());
        Set<EditGraphEdge> outgoingEdges = Sets.newHashSet(editGraphVertex.outgoing());
        for (EditGraphEdge incomingEdge : incomingEdges) {
          for (EditGraphEdge outgoingEdge : outgoingEdges) {
            editGraph.connect(incomingEdge.from(), outgoingEdge.to(), EditOperation.GAP, incomingEdge.getScore() + outgoingEdge.getScore());
          }
        }
        for (EditGraphEdge e : incomingEdges) {
          e.delete();
        }
        for (EditGraphEdge e : outgoingEdges) {
          e.delete();
        }
      }
    }
    return newEditGraph;
  }

  private LinkedHashSet<EditGraphEdge> getMinimumScoreEdges(Matches matches) {
    Set<Token> ambiguous = matches.getAmbiguous();
    LinkedHashSet<EditGraphEdge> edgeSet1 = Sets.newLinkedHashSet();
    List<EditGraphVertex> verticesToCheck = Lists.newArrayList(editGraph.getStart());
    boolean onePath = true;
    while (!verticesToCheck.isEmpty()) {
      EditGraphVertex vertex = verticesToCheck.remove(0);
      Set<EditGraphEdge> outgoingEdges = Sets.newHashSet(vertex.outgoing());
      if (!outgoingEdges.isEmpty()) {
        Multimap<Integer, EditGraphEdge> edgesForScore = ArrayListMultimap.create();
        for (EditGraphEdge editGraphEdge : outgoingEdges) {
          edgesForScore.put(editGraphEdge.getScore(), editGraphEdge);
        }
        Integer minScore = Collections.min(edgesForScore.keySet());
        Collection<EditGraphEdge> edges = edgesForScore.get(minScore);
        onePath = onePath && (edges.size() == 1);
        edgeSet1.addAll(edges);
        for (EditGraphEdge editGraphEdge : edges) {
          verticesToCheck.add(editGraphEdge.to());
        }
      }
    }

    if (!onePath) {
      LOG.info("try reverse");
      LinkedHashSet<EditGraphEdge> edgeSet2 = Sets.newLinkedHashSet();
      verticesToCheck = Lists.newArrayList(editGraph.getEnd());
      while (!verticesToCheck.isEmpty()) {
        EditGraphVertex vertex = verticesToCheck.remove(0);
        Set<EditGraphEdge> incomingEdges = Sets.newHashSet(vertex.incoming());
        if (!incomingEdges.isEmpty()) {
          Multimap<Integer, EditGraphEdge> edgesForScore = ArrayListMultimap.create();
          for (EditGraphEdge editGraphEdge : incomingEdges) {
            edgesForScore.put(editGraphEdge.getScore(), editGraphEdge);
          }
          Integer minScore = Collections.min(edgesForScore.keySet());
          Collection<EditGraphEdge> edges = edgesForScore.get(minScore);
          edgeSet2.addAll(edges);
          for (EditGraphEdge editGraphEdge : edges) {
            verticesToCheck.add(editGraphEdge.from());
          }
        }
      }
      return Sets.newLinkedHashSet(Sets.intersection(edgeSet1, edgeSet2));
    }

    return edgeSet1;
  }

  private Set<EditGraphVertex> getVertices(Set<EditGraphEdge> minimumScoreEdges) {
    Set<EditGraphVertex> vertices = Sets.newLinkedHashSet();
    for (EditGraphEdge edge : minimumScoreEdges) {
      vertices.add(edge.from());
      vertices.add(edge.to());
    }
    return vertices;
  }

  private EditGraph buildNewGraphWithOnlyMinimumWeightVertices(Map<EditGraphVertex, Integer> minWeightProVertex, Matches match) {
    // par: Map<EditGraphEdge,Integer> minSequencesForEdge, matches
    Set<EditGraphVertex> verticesToKeep = getMinimumWeightVertices(minWeightProVertex, match);
    Set<EditGraphEdge> newEdges = getEdges(verticesToKeep);

    return newEditGraph(newEdges, verticesToKeep);
  }

  private Set<EditGraphEdge> getEdges(Set<EditGraphVertex> verticesToKeep) {
    Set<EditGraphEdge> edgeSet = Sets.newHashSet(editGraph.edges());
    Set<EditGraphEdge> newEdges = Sets.newLinkedHashSet();
    for (EditGraphEdge edge : edgeSet) {
      if (verticesToKeep.contains(edge.from()) && verticesToKeep.contains(edge.to())) {
        newEdges.add(editGraph.connect(edge.from(), edge.to(), edge.getEditOperation(), edge.getScore()));
      }
    }
    return newEdges;
  }

  private Set<EditGraphVertex> getMinimumWeightVertices(Map<EditGraphVertex, Integer> minWeightProVertex, Matches match) {
    Set<EditGraphVertex> verticesToKeep = Sets.newLinkedHashSet();
    Set<Token> ambiguous = match.getAmbiguous();
    //    Set<INormalizedToken> unique = match.getUnique();

    List<EditGraphVertex> verticesInReverseTopologicalOrder = Lists.reverse(getVerticesInTopologicalOrder(editGraph));

    int localminimum = minWeightProVertex.get(editGraph.getEnd());
    for (EditGraphVertex vertex : verticesInReverseTopologicalOrder) {
      Token token = vertex.getWitness();
      Integer minWeight = minWeightProVertex.get(vertex);
      if (ambiguous.contains(token)) {
        if (minWeight <= localminimum) {
          vertex.setWeight(minWeight);
          verticesToKeep.add(vertex);
        }
      } else {
        vertex.setWeight(minWeight);
        verticesToKeep.add(vertex);
        localminimum = minWeight;
      }
    }
    return verticesToKeep;
  }

  private EditGraph newEditGraph(Set<EditGraphEdge> edgesToKeep, Set<EditGraphVertex> newVertices) {
    throw new UnsupportedOperationException();
    /*
    EditGraph newGraph = new EditGraph();
    newGraph.setStartVertex(editGraph.getStartVertex());
    newGraph.setEndVertex(editGraph.getEndVertex());
    for (EditGraphVertex vertex : newVertices) {
      newGraph.addVertex(vertex);
    }
    for (EditGraphEdge edge : edgesToKeep) {
      newGraph.add(edge);
    }
    return newGraph;
    */
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
    Set<EditGraphEdge> outgoingEdgesOf = Sets.newHashSet(vertex.outgoing());
    if (outgoingEdgesOf.isEmpty()) {
      throw new RuntimeException("Error: " + vertex.toString() + " has no outgoing edges!");
    }
    Map<EditGraphEdge, Integer> edges = Maps.newLinkedHashMap();
    for (EditGraphEdge outgoing : outgoingEdgesOf) {
      EditGraphVertex targetVertex = outgoing.to();
      // Note: Gaps causes extra sequences.
      // Note: A sequence at the start vertex does not count, cause
      // Note: it will be an empty sequence, cause there are no more tokens
      if (vertex != graph.getStart() && gapOrNoGap.get(targetVertex) == 0 && outgoing.getEditOperation() == EditOperation.GAP) {
        edges.put(outgoing, 1 + minSeq.get(targetVertex));
      } else {
        edges.put(outgoing, minSeq.get(targetVertex));
      }
    }
    return edges;
  }
}
