package eu.interedition.collatex2.implementation.vg_analysis;

import java.util.List;

public class Analysis implements IAnalysis {
  private final List<ISequence> sequences;

  public Analysis(List<ISequence> sequences) {
    this.sequences = sequences;
  }
  
  @Override
  public List<ISequence> getSequences() {
    return sequences;
  }
  

}
