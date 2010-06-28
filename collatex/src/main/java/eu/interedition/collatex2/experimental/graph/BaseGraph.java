package eu.interedition.collatex2.experimental.graph;

import java.util.Collection;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;

import com.google.common.collect.Sets;

public class BaseGraph implements Graph<IVariantGraphVertex, IVariantGraphEdge> {
  private final Set<IVariantGraphEdge>  edges;
  private final Set<IVariantGraphVertex> vertices;

  public BaseGraph() {
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

  //TODO: create vertex with normalized form on VariantGraphClass
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

  //TODO: not a nice implementation; see remark!
  //TODO: use outgoingEdges method instead!
  @Override
  public Set<IVariantGraphEdge> edgesOf(IVariantGraphVertex vertex) {
    Set<IVariantGraphEdge> results = Sets.newLinkedHashSet();
    for (IVariantGraphEdge edge : edges) {
      if (edge.getBeginVertex().equals(vertex) /*|| edge.getEndVertex().equals(vertex)*/) {
        results.add(edge);
      }
    }
    return results;
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

}