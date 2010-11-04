package eu.interedition.collatex2.implementation.vg_analysis;

import eu.interedition.collatex2.interfaces.IPhrase;

public interface ISequence {

  String getNormalized();
  
  IPhrase getBasePhrase();
  
  IPhrase getWitnessPhrase();

}
