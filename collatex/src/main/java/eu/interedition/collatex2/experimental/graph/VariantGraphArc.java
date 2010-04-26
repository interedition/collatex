package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphArc implements IVariantGraphArc {

  private final IVariantGraphNode start;
  private final IVariantGraphNode end;
  private final List<IWitness> witnesses;

  public VariantGraphArc(IVariantGraphNode start, IVariantGraphNode end, IWitness witness) {
    this.start = start;
    this.end = end;
    this.witnesses = Lists.newArrayList(witness);
  }

  public List<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public IVariantGraphNode getBeginNode() {
    return start;
  }

  @Override
  public IVariantGraphNode getEndNode() {
    return end;
  }
  
  @Override
  public String toString() {
    String splitter="";
    String to = getBeginNode().getNormalized()+" -> "+getEndNode().getNormalized()+": ";
    for (IWitness witness: witnesses) {
      to += splitter+witness.getSigil();
      splitter=", ";
    }
    return to;
  }

}
