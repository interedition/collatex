package eu.interedition.collatex2.experimental.graph;

import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentArc implements IAlignmentArc {

  private final IAlignmentNode start;
  private final IAlignmentNode end;
  private final IWitness witness;

  public AlignmentArc(IAlignmentNode start, IAlignmentNode end, IWitness witness) {
    this.start = start;
    this.end = end;
    this.witness = witness;
  }

  public IWitness getWitness() {
    return witness;
  }

  @Override
  public IAlignmentNode getBeginNode() {
    return start;
  }

  @Override
  public IAlignmentNode getEndNode() {
    return end;
  }

}
