package eu.interedition.collatex2.experimental.graph;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IModifiableVariantGraph extends IVariantGraph {

  IVariantGraphVertex addNewVertex(INormalizedToken token, IWitness a);

  void addWitness(IWitness a);

}
