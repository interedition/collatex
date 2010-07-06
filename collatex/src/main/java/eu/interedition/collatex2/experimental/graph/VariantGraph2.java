package eu.interedition.collatex2.experimental.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.graph.indexing.IVariantGraphIndex;
import eu.interedition.collatex2.experimental.graph.indexing.VariantGraphIndexMatcher;
import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

// This class implements the IVariantGraph interface.
// The IVariantGraph interface is an extension of the DiGraph interface
// The implementation is based on a DAG.
// The VariantGraph contains a start and an end vertex.
// The VariantGraph contains a List of witnesses that have
// been added to the Graph.
@SuppressWarnings("serial")
public class VariantGraph2 extends DirectedAcyclicGraph<IVariantGraphVertex, IVariantGraphEdge> implements IVariantGraph {
  private final IVariantGraphVertex startVertex;
  private final IVariantGraphVertex endVertex;
  private final List<IWitness> witnesses;

  private VariantGraph2() {
    super(IVariantGraphEdge.class);
    this.witnesses = Lists.newArrayList();
    startVertex = new VariantGraphVertex(new NullToken(0, null));
    addVertex(startVertex);
    endVertex = new VariantGraphVertex(new NullToken(0, null));
    addVertex(endVertex);
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
  @Override
    public void addWitness(IWitness witness) {
      witnesses.add(witness);
      VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(this);
      List<ITokenMatch> matches = matcher.getMatches(witness);
      makeEdgesForMatches(witness, matches, matcher.getGraphIndex());
  }

  //TODO: implement!
  @Override
  public List<String> findRepeatingTokens() {
    return Lists.newArrayList();
  }

  @Override
  public IVariantGraphVertex getEndVertex() {
    return endVertex;
  }

 
  @Override
  public IVariantGraphVertex getStartVertex() {
    return startVertex;
  }

  @Override
  public List<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public boolean isEmpty() {
    return witnesses.isEmpty();
  }

  public static VariantGraph2 create() {
    return new VariantGraph2();
  }

  public List<IVariantGraphVertex> getPath(IWitness witness) {
    List<IVariantGraphVertex> path = Lists.newArrayList();
    IVariantGraphVertex startVertex = getStartVertex();
    IVariantGraphVertex currentVertex = startVertex;
    while (outDegreeOf(currentVertex) > 0) {
      Set<IVariantGraphEdge> outgoingEdges = outgoingEdgesOf(currentVertex);
      boolean found = false;
      for (IVariantGraphEdge edge : outgoingEdges) {
        if (!found&&edge.containsWitness(witness)) {
          found = true;
          IVariantGraphVertex edgeTarget = getEdgeTarget(edge);
          if (!edgeTarget.getNormalized().equals("#")) {
            path.add(edgeTarget);
          }
          currentVertex = edgeTarget;
        }
      }
      if (!found) {
        throw new RuntimeException("No valid path found for "+witness.getSigil());
      }
    }
    return path;
  }

  //TODO: should the first witness really be a special case like this?
  public static IVariantGraph create(IWitness a) {
    VariantGraph2 graph = create();
    //TODO: this is not very nice!
    //TODO: make getWitnesses read only!
    graph.getWitnesses().add(a);
    List<IVariantGraphVertex> newVertices = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      newVertices.add(graph.addNewVertex(token, a));
    }
    IVariantGraphVertex previous = graph.getStartVertex();
    for (IVariantGraphVertex vertex : newVertices) {
      graph.addNewEdge(previous, vertex, a);
      previous = vertex;
    }
    graph.addNewEdge(previous, graph.getEndVertex(), a);
    return graph;
  }
  
  //write
  private IVariantGraphVertex addNewVertex(INormalizedToken token, IWitness w) {
    final VariantGraphVertex vertex = new VariantGraphVertex(token);
    addVertex(vertex);
    //TODO: is this if still necessary?
    if (w!=null) {
      vertex.addToken(w, token);
    }
    return vertex;
  }
  
  //write
  private void addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    IVariantGraphEdge e = new VariantGraphEdge(begin, end, witness);
    addEdge(begin, end, e);
  }
  
  //write
  private void makeEdgesForMatches(IWitness witness, List<ITokenMatch> matches, IVariantGraphIndex graphIndex2) {
    Map<INormalizedToken, ITokenMatch> witnessTokenToMatch;
    witnessTokenToMatch = Maps.newLinkedHashMap();
    for (ITokenMatch match : matches) {
      INormalizedToken tokenA = match.getTokenA();
      witnessTokenToMatch.put(tokenA, match);
    }
    IVariantGraphVertex begin = this.getStartVertex();
    for (INormalizedToken token : witness.getTokens()) {
      if (!witnessTokenToMatch.containsKey(token)) {
        // NOTE: here we determine that the token is an addition/replacement!
        IVariantGraphVertex end = this.addNewVertex(token, witness);
        addNewEdge(begin, end, witness);
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
    IVariantGraphVertex end = getEndVertex();
    connectBeginToEndVertex(begin, end, witness);
  }

  // write
  private void connectBeginToEndVertex(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    if (containsEdge(begin, end)) {
      IVariantGraphEdge existingEdge = getEdge(begin, end);
      existingEdge.addWitness(witness);
    } else {
      addNewEdge(begin, end, witness);
    }
  }

  @Override
  public List<IVariantGraphVertex> getLongestPath() {
      // NOTE: Weights are set to negative value to
      // generate the longest path instead of the shortest path
      for (IVariantGraphEdge edge : edgeSet()) {
        setEdgeWeight(edge, -1);
      }
      // NOTE: gets the start vertex of the graph
      IVariantGraphVertex startVertex = getStartVertex();
      IVariantGraphVertex endVertex = getEndVertex();
      // Note: calculates the longest path
      List<IVariantGraphEdge> findPathBetween = BellmanFordShortestPath.findPathBetween(this, startVertex, endVertex);
      // Note: gets the end vertices associated with the edges of the path
      List<IVariantGraphVertex> vertices = Lists.newArrayList();
      for (IVariantGraphEdge edge : findPathBetween) {
        IVariantGraphVertex edgeTarget = this.getEdgeTarget(edge);
        if (edgeTarget != endVertex) {
          vertices.add(edgeTarget);
        }
      }
      return vertices;
    }
}
