package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphNode implements IVariantGraphNode {
  private final INormalizedToken token;
  private final List<IVariantGraphArc>  arcs;

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
  public List<IVariantGraphArc> getArcs() {
    return arcs;
  }

  @Override
  public void addNewArc(IVariantGraphNode end, IWitness witness, INormalizedToken token) {
    IVariantGraphArc arc = new VariantGraphArc(this, end, witness, token);
    arcs.add(arc);
  }

  @Override
  public boolean arcExist(IVariantGraphNode end) {
    for (IVariantGraphArc arc : arcs) {
      if (arc.getBeginNode().equals(this) && arc.getEndNode().equals(end)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public IVariantGraphArc find(IVariantGraphNode end) {
    for (IVariantGraphArc arc : arcs) {
      if (arc.getBeginNode().equals(this) && arc.getEndNode().equals(end)) {
        return arc;
      }
    }
    throw new RuntimeException("Arc '" + this.getNormalized() + "' -> '" + this.getNormalized() + "' not found!");
  }

  @Override
  public IVariantGraphArc findArc(IWitness witness) {
    for (IVariantGraphArc arc : arcs) {
      if (arc.getBeginNode().equals(this) && arc.getWitnesses().contains(witness)) {
        return arc;
      }
    }
    return null;
  }

  @Override
  public boolean hasArc(IWitness witness) {
    return findArc(witness) != null;
  }
  


}
