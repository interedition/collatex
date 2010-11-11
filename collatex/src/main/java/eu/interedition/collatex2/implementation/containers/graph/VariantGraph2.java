package eu.interedition.collatex2.implementation.containers.graph;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
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

  private VariantGraph2() {
    super(IVariantGraphEdge.class);
    startVertex = new VariantGraphVertex("#", null);
    addVertex(startVertex);
    endVertex = new VariantGraphVertex("#", null);
    addVertex(endVertex);
  }

  @Override
  public List<String> getRepeatedTokens() {
    // remove start and end vertices
    Set<IVariantGraphVertex> copy = Sets.newLinkedHashSet(vertexSet());
    copy.remove(startVertex);
    copy.remove(endVertex);
    // we map all vertices to their normalized version
    Multimap<String, IVariantGraphVertex> mapped = ArrayListMultimap.create();
    for (IVariantGraphVertex v : copy) {
      mapped.put(v.getNormalized(), v);
    }
    // fetch all the duplicate keys and return them 
    List<String> result = Lists.newArrayList();
    for (String key : mapped.keySet()) {
      if (mapped.get(key).size() > 1) {
        result.add(key);
      }
    }
    return result;
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
    Set<IVariantGraphEdge> outgoingEdges = outgoingEdgesOf(startVertex);
    List<IWitness> totalWitnesses = Lists.newArrayList();
    for (IVariantGraphEdge edge : outgoingEdges) {
      totalWitnesses.addAll(edge.getWitnesses());
    }
    //NOTE: set of outgoingEdges in unordered!
    //NOTE: so the list of witnesses is sorted here!
    Collections.sort(totalWitnesses, new Comparator<IWitness>() {
      @Override
      public int compare(IWitness arg0, IWitness arg1) {
        return arg0.getSigil().compareTo(arg1.getSigil());
      } });
    return Collections.unmodifiableList(totalWitnesses);
  }

  @Override
  public boolean isEmpty() {
    return getWitnesses().isEmpty();
  }

  public static VariantGraph2 create() {
    return new VariantGraph2();
  }

  //TODO: should the first witness really be a special case like this?
  public static VariantGraph2 create(IWitness a) {
    VariantGraph2 graph = VariantGraph2.create();
    List<IVariantGraphVertex> newVertices = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      final IVariantGraphVertex vertex = graph.addNewVertex(token.getNormalized(), token);
      vertex.addToken(a, token);
      newVertices.add(vertex);
    }
    IVariantGraphVertex previous = graph.getStartVertex();
    for (IVariantGraphVertex vertex : newVertices) {
      graph.addNewEdge(previous, vertex, a);
      previous = vertex;
    }
    graph.addNewEdge(previous, graph.getEndVertex(), a);
    return graph;
  }

  @Override
  public List<IVariantGraphVertex> getPath(IWitness witness) {
    List<IVariantGraphVertex> path = Lists.newArrayList();
    IVariantGraphVertex startVertex = getStartVertex();
    IVariantGraphVertex currentVertex = startVertex;
    while (outDegreeOf(currentVertex) > 0) {
      Set<IVariantGraphEdge> outgoingEdges = outgoingEdgesOf(currentVertex);
      boolean found = false;
      for (IVariantGraphEdge edge : outgoingEdges) {
        if (!found && edge.containsWitness(witness)) {
          found = true;
          IVariantGraphVertex edgeTarget = getEdgeTarget(edge);
          if (!edgeTarget.getNormalized().equals("#")) {
            path.add(edgeTarget);
          }
          currentVertex = edgeTarget;
        }
      }
      if (!found) {
        throw new RuntimeException("No valid path found for " + witness.getSigil());
      }
    }
    return path;
  }

  //write
  public IVariantGraphVertex addNewVertex(String normalized, INormalizedToken vertexKey) {
    final VariantGraphVertex vertex = new VariantGraphVertex(normalized, vertexKey);
    addVertex(vertex);
    return vertex;
  }

  //write
  public void addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    IVariantGraphEdge e = new VariantGraphEdge(begin, end, witness);
    addEdge(begin, end, e);
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

  @Override
  public ITokenIndex getTokenIndex(List<String> repeatingTokens) {
    return VariantGraphIndex.create(this, repeatingTokens);
  }

  @Override
  public List<INormalizedToken> getTokens(IWitness witness) {
    List<IVariantGraphVertex> vertices = getPath(witness);
    List<INormalizedToken> tokens = Lists.newArrayList();
    for (IVariantGraphVertex vertex : vertices) {
      tokens.add(vertex);
    }
    return tokens;
  }

  @Override
  public boolean isNear(IToken a, IToken b) {
    // sanity check!
    if (!(a instanceof IVariantGraphVertex)) {
      throw new RuntimeException("IToken a is not of type IVariantGraphVertex!");
    }
    if (!(b instanceof IVariantGraphVertex)) {
      throw new RuntimeException("IToken b is not of type IVariantGraphVertex!");
    }
    return containsEdge((IVariantGraphVertex) a, (IVariantGraphVertex) b);
  }

  @Override
  public Iterator<INormalizedToken> tokenIterator() {
    final Iterator<IVariantGraphVertex> verticesIterator = iterator();
    return new Iterator<INormalizedToken>() {
      @Override
      public boolean hasNext() {
        return verticesIterator.hasNext();
      }

      @Override
      public INormalizedToken next() {
        IVariantGraphVertex vertex = verticesIterator.next();
        return vertex;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
