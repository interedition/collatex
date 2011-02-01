package eu.interedition.collatex2.implementation.output.segmented_graph;

import java.util.List;

import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;


public interface ISegmentedVariantGraphVertex {

  String getNormalized();

  List<IWitness> getWitnesses();

  IPhrase getPhrase(IWitness witness);

}
