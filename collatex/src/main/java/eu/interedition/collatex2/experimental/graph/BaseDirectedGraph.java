package eu.interedition.collatex2.experimental.graph;

import java.util.Collection;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;

import com.google.common.collect.Sets;

public class BaseDirectedGraph implements DirectedGraph<IVariantGraphVertex, IVariantGraphEdge> {
  private final Set<IVariantGraphEdge>  edges;
  private final Set<IVariantGraphVertex> vertices;

  public BaseDirectedGraph() {
    this.edges = Sets.newLinkedHashSet();
    this.vertices = Sets.newLinkedHashSet();
  }
  
  @Override
  public VariantGraphEdge addEdge(IVariantGraphVertex sourceVertex, IVariantGraphVertex targetVertex) {
    throw new RuntimeException("addEdge (source, target)");
  }

  @Override
  public boolean addEdge(IVariantGraphVertex sourceVertex, IVariantGraphVertex targetVertex, IVariantGraphEdge e) {
    return edges.add(e);
  }

  @Override
  public boolean addVertex(IVariantGraphVertex vertex) {
    return vertices.add(vertex);
  }

  @Override
  public boolean containsEdge(IVariantGraphEdge e) {
    return edges.contains(e);
  }

  @Override
  public boolean containsEdge(IVariantGraphVertex sourceVertex, IVariantGraphVertex targetVertex) {
    for (IVariantGraphEdge edge : edges) {
      if (edge.getBeginVertex().equals(sourceVertex) && edge.getEndVertex().equals(targetVertex)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsVertex(IVariantGraphVertex v) {
    return vertices.contains(v);
  }

  @Override
  public Set<IVariantGraphEdge> edgeSet() {
    return edges;
  }

  @Override
  public Set<IVariantGraphEdge> edgesOf(IVariantGraphVertex vertex) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
  }

  @Override
  public Set<IVariantGraphEdge> getAllEdges(IVariantGraphVertex sourceVertex, IVariantGraphVertex targetVertex) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
  }

  @Override
  public VariantGraphEdge getEdge(IVariantGraphVertex sourceVertex, IVariantGraphVertex targetVertex) {
    for (IVariantGraphEdge edge : edges) {
      if (edge.getBeginVertex().equals(sourceVertex) && edge.getEndVertex().equals(targetVertex)) {
        return (VariantGraphEdge) edge;
      }
    }
    throw new RuntimeException("Edge '" + sourceVertex.getNormalized() + "' -> '" + targetVertex.getNormalized() + "' not found!");
  }

  @Override
  public EdgeFactory<IVariantGraphVertex, IVariantGraphEdge> getEdgeFactory() {
    throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
  }

  @Override
  public VariantGraphVertex getEdgeSource(IVariantGraphEdge e) {
    return (VariantGraphVertex) e.getBeginVertex();
  }

  @Override
  public VariantGraphVertex getEdgeTarget(IVariantGraphEdge e) {
    return (VariantGraphVertex) e.getEndVertex();
  }

  @Override
  public double getEdgeWeight(IVariantGraphEdge e) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
  }

  @Override
  public boolean removeAllEdges(Collection<? extends IVariantGraphEdge> edges) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
  }

  @Override
  public Set<IVariantGraphEdge> removeAllEdges(IVariantGraphVertex sourceVertex, IVariantGraphVertex targetVertex) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
  }

  @Override
  public boolean removeAllVertices(Collection<? extends IVariantGraphVertex> vertices) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
  }

  @Override
  public boolean removeEdge(IVariantGraphEdge e) {
    return edges.remove(e);
  }

  @Override
  public VariantGraphEdge removeEdge(IVariantGraphVertex sourceVertex, IVariantGraphVertex targetVertex) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
  }

  @Override
  public boolean removeVertex(IVariantGraphVertex v) {
    return vertices.remove(v);
  }

  @Override
  public Set<IVariantGraphVertex> vertexSet() {
    return vertices;
  } 

  @Override
  public int inDegreeOf(IVariantGraphVertex vertex) {
    return incomingEdgesOf(vertex).size();
  }

  @Override
  public Set<IVariantGraphEdge> incomingEdgesOf(IVariantGraphVertex vertex) {
    Set<IVariantGraphEdge> results = Sets.newLinkedHashSet();
    for (IVariantGraphEdge edge : edges) {
      if (edge.getEndVertex().equals(vertex)) {
        results.add(edge);
      }
    }
    return results;
  }

  @Override
  public int outDegreeOf(IVariantGraphVertex vertex) {
    return outgoingEdgesOf(vertex).size();
  }

  @Override
  public Set<IVariantGraphEdge> outgoingEdgesOf(IVariantGraphVertex vertex) {
    Set<IVariantGraphEdge> results = Sets.newLinkedHashSet();
    for (IVariantGraphEdge edge : edges) {
      if (edge.getBeginVertex().equals(vertex)) {
        results.add(edge);
      }
    }
    return results;
  }
}