package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.experimental.table.CollateXVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphVertex extends CollateXVertex implements IVariantGraphVertex {
  private final INormalizedToken token;
  private final List<IVariantGraphEdge>  edges;

  public VariantGraphVertex(INormalizedToken token) {
    super(token.getNormalized());
    this.token = token;
    this.edges = Lists.newArrayList();
  }

  @Override
  public String getNormalized() {
    return token.getNormalized();
  }

  @Override
  public List<IVariantGraphEdge> getEdges() {
    return edges;
  }

  @Override
  public void addNewEdge(IVariantGraphVertex end, IWitness witness) {
    IVariantGraphEdge edge = new VariantGraphEdge(this, end, witness);
    edges.add(edge);
  }

  @Override
  public boolean hasEdge(IVariantGraphVertex end) {
    for (IVariantGraphEdge arc : edges) {
      if (arc.getBeginVertex().equals(this) && arc.getEndVertex().equals(end)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public IVariantGraphEdge findEdge(IVariantGraphVertex end) {
    for (IVariantGraphEdge arc : edges) {
      if (arc.getBeginVertex().equals(this) && arc.getEndVertex().equals(end)) {
        return arc;
      }
    }
    throw new RuntimeException("Arc '" + this.getNormalized() + "' -> '" + this.getNormalized() + "' not found!");
  }

  @Override
  public IVariantGraphEdge findEdge(IWitness witness) {
    for (IVariantGraphEdge arc : edges) {
      if (arc.getBeginVertex().equals(this) && arc.getWitnesses().contains(witness)) {
        return arc;
      }
    }
    return null;
  }

  @Override
  public boolean hasEdge(IWitness witness) {
    return findEdge(witness) != null;
  }
}
