package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentArc implements IAlignmentArc {

  private final IAlignmentNode start;
  private final IAlignmentNode end;
  private final List<IWitness> witnesses;

  public AlignmentArc(IAlignmentNode start, IAlignmentNode end, IWitness witness) {
    this.start = start;
    this.end = end;
    this.witnesses = Lists.newArrayList(witness);
  }

  public List<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public IAlignmentNode getBeginNode() {
    return start;
  }

  @Override
  public IAlignmentNode getEndNode() {
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
