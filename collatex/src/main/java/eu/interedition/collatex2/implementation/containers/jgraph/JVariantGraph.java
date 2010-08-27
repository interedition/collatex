package eu.interedition.collatex2.implementation.containers.jgraph;

import java.util.Collection;
import java.util.Set;

import org.jgrapht.EdgeFactory;

import eu.interedition.collatex2.interfaces.IJVariantGraph;
import eu.interedition.collatex2.interfaces.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;

public class JVariantGraph implements IJVariantGraph {

  public static IJVariantGraph create() {
    return new JVariantGraph();
  }

  @Override
  public int inDegreeOf(IJVariantGraphVertex arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Set<IJVariantGraphEdge> incomingEdgesOf(IJVariantGraphVertex arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int outDegreeOf(IJVariantGraphVertex arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Set<IJVariantGraphEdge> outgoingEdgesOf(IJVariantGraphVertex arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IJVariantGraphEdge addEdge(IJVariantGraphVertex arg0, IJVariantGraphVertex arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean addEdge(IJVariantGraphVertex arg0, IJVariantGraphVertex arg1, IJVariantGraphEdge arg2) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean addVertex(IJVariantGraphVertex arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean containsEdge(IJVariantGraphEdge arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean containsEdge(IJVariantGraphVertex arg0, IJVariantGraphVertex arg1) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean containsVertex(IJVariantGraphVertex arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Set<IJVariantGraphEdge> edgeSet() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<IJVariantGraphEdge> edgesOf(IJVariantGraphVertex arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<IJVariantGraphEdge> getAllEdges(IJVariantGraphVertex arg0, IJVariantGraphVertex arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IJVariantGraphEdge getEdge(IJVariantGraphVertex arg0, IJVariantGraphVertex arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EdgeFactory<IJVariantGraphVertex, IJVariantGraphEdge> getEdgeFactory() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IJVariantGraphVertex getEdgeSource(IJVariantGraphEdge arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IJVariantGraphVertex getEdgeTarget(IJVariantGraphEdge arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getEdgeWeight(IJVariantGraphEdge arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean removeAllEdges(Collection<? extends IJVariantGraphEdge> arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Set<IJVariantGraphEdge> removeAllEdges(IJVariantGraphVertex arg0, IJVariantGraphVertex arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean removeAllVertices(Collection<? extends IJVariantGraphVertex> arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean removeEdge(IJVariantGraphEdge arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public IJVariantGraphEdge removeEdge(IJVariantGraphVertex arg0, IJVariantGraphVertex arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean removeVertex(IJVariantGraphVertex arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Set<IJVariantGraphVertex> vertexSet() {
    // TODO Auto-generated method stub
    return null;
  }

}
