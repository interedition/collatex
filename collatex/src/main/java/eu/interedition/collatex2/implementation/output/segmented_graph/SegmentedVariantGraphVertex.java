package eu.interedition.collatex2.implementation.output.segmented_graph;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

public class SegmentedVariantGraphVertex implements
    ISegmentedVariantGraphVertex {

  private final Map<IWitness, IPhrase> phraseForEachWitness;

  public SegmentedVariantGraphVertex(Map<IWitness, IPhrase> phraseForEachWitness) {
    this.phraseForEachWitness = phraseForEachWitness;
  }

  @Override
  public String getNormalized() {
    if (phraseForEachWitness.isEmpty()) {
      return "#";
    }
    return phraseForEachWitness.values().iterator().next().getNormalized();
  }

  //NOTE: should this be a list?
  @Override
  public List<IWitness> getWitnesses() {
    return Lists.newArrayList(phraseForEachWitness.keySet());
  }

  @Override
  public IPhrase getPhrase(IWitness witness) {
    return phraseForEachWitness.get(witness);
  }
 
}
