package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.interfaces.IWitness;

public abstract class VariantGraph implements IVariantGraph {
  private final IVariantGraphVertex startVertex;
  private final IVariantGraphVertex endVertex;

  // read
  @Override
  public IVariantGraphVertex getStartVertex() {
    return startVertex;
  }

  // read
  @Override
  public IVariantGraphVertex getEndVertex() {
    return endVertex;
  }

  // read
  // NOTE: only for testing purposes!
  @Override
  public List<IVariantGraphEdge> getEdges() {
    List<IVariantGraphEdge> allEdges = Lists.newArrayList();
    for (IVariantGraphVertex vertex : getVertices()) {
      allEdges.addAll(vertex.getEdges());
    }
    return allEdges;
  }

  // constructor
  // TODO: why does NullToken need a sigil as parameter?
  protected VariantGraph() {
    this.startVertex = new VariantGraphVertex(new NullToken(1, null));
    this.endVertex = new VariantGraphVertex(new NullToken(1, null));
  }

  // read (for indexing)
  // TODO Auto-generated method stub
  @Override
  public List<String> findRepeatingTokens() {
    return Lists.newArrayList();
  }

  // read
  @Override
  public boolean isEmpty() {
    return getWitnesses().isEmpty();
  }

  // read
  public List<IVariantGraphEdge> getArcsForWitness(IWitness witness) {
    IVariantGraphVertex node = getStartVertex();
    List<IVariantGraphEdge> arcs = Lists.newArrayList();
    while (node.hasEdge(witness)) {
      final IVariantGraphEdge arc = node.findEdge(witness);
      arcs.add(arc);
      node = arc.getEndVertex();
    }
    return arcs;
  }

  //TODO: remove this method!
  // read
  public List<IVariantGraphVertex> getPath(IWitness witness) {
    IVariantGraphVertex beginNode = getStartVertex();
    List<IVariantGraphVertex> path = Lists.newArrayList();
    while (beginNode.hasEdge(witness)) {
      IVariantGraphEdge arc = beginNode.findEdge(witness);
      IVariantGraphVertex endNode = arc.getEndVertex();
      path.add(endNode);
      beginNode = endNode;
    }
    return path;
  }

}
