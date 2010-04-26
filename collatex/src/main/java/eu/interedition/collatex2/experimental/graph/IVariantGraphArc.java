package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraphArc {

  IVariantGraphNode getBeginNode();

  IVariantGraphNode getEndNode();

  List<IWitness> getWitnesses();

}
