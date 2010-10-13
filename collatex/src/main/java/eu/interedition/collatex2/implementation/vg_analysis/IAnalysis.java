package eu.interedition.collatex2.implementation.vg_analysis;

import java.util.List;


public interface IAnalysis {

  // purpose: detect sequences in the token matches and combine
  // them together into sequences
  List<ISequence> getSequences();
  
  List<ITransposition2> getTranspositions();

}
