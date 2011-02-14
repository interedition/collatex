package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.List;

import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IAlignment2 {

  List<ITokenMatch> getTokenMatches();
  
  IWitness getWitness();

  IVariantGraph getGraph();

}
