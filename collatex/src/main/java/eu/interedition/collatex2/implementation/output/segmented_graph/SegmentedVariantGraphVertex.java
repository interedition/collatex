package eu.interedition.collatex2.implementation.output.segmented_graph;

import java.util.Map;

import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

public class SegmentedVariantGraphVertex implements
    ISegmentedVariantGraphVertex {

  private final Map<IWitness, IPhrase> phraseForEachWitness;

  public SegmentedVariantGraphVertex(Map<IWitness, IPhrase> phraseForEachWitness) {
    this.phraseForEachWitness = phraseForEachWitness;
  }

}
