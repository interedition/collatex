package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphNode implements IVariantGraphNode {
  private final INormalizedToken token;
  private final List<IVariantGraphEdge>  arcs;

  public VariantGraphNode(INormalizedToken token) {
    this.token = token;
    this.arcs = Lists.newArrayList();
  }

  @Override
  public String getNormalized() {
    return token.getNormalized();
  }

  @Override
  public INormalizedToken getToken() {
    return token;
  }

  @Override
  public List<IVariantGraphEdge> getEdges() {
    return arcs;
  }

  @Override
  public void addNewEdge(IVariantGraphNode end, IWitness witness, INormalizedToken token) {
    IVariantGraphEdge arc = new VariantGraphEdge(this, end, witness, token);
    arcs.add(arc);
  }

  @Override
  public boolean hasEdge(IVariantGraphNode end) {
    for (IVariantGraphEdge arc : arcs) {
      if (arc.getBeginNode().equals(this) && arc.getEndNode().equals(end)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public IVariantGraphEdge findEdge(IVariantGraphNode end) {
    for (IVariantGraphEdge arc : arcs) {
      if (arc.getBeginNode().equals(this) && arc.getEndNode().equals(end)) {
        return arc;
      }
    }
    throw new RuntimeException("Arc '" + this.getNormalized() + "' -> '" + this.getNormalized() + "' not found!");
  }

  @Override
  public IVariantGraphEdge findEdge(IWitness witness) {
    for (IVariantGraphEdge arc : arcs) {
      if (arc.getBeginNode().equals(this) && arc.getWitnesses().contains(witness)) {
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
