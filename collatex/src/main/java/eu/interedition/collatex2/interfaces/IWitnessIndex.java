package eu.interedition.collatex2.interfaces;

import java.util.Collection;
import java.util.Set;

public interface IWitnessIndex {

  boolean contains(String normalized);

  int size();

  // returns all the normalized phrases contained in this index
  Set<String> keys();
  
  Collection<IPhrase> getPhrases();

  IPhrase getPhrase(String normalized);

}
