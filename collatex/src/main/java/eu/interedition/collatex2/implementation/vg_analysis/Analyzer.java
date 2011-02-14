package eu.interedition.collatex2.implementation.vg_analysis;

import java.util.List;

import eu.interedition.collatex2.implementation.vg_alignment.IAlignment2;
import eu.interedition.collatex2.interfaces.IVariantGraph;

public class Analyzer {

  public IAnalysis analyze(IAlignment2 alignment) {
    SequenceDetection2 seqDetection = new SequenceDetection2(alignment);
    List<ISequence> sequences = seqDetection.chainTokenMatches();
    IVariantGraph graph = alignment.getGraph();
    IAnalysis analysis = new Analysis(sequences, graph);
    return analysis;
  }

}
