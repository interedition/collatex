package eu.interedition.collatex2.interfaces;

import java.util.Set;

public interface IWitnessIndex {

  boolean contains(String normalized);

  int size();

  // returns all the normalized phrases contained in this index
  Set<String> keys();
  
  //NOTE: refactor return value to List<T>?
  IPhrase getPhrase(String normalized);

}
