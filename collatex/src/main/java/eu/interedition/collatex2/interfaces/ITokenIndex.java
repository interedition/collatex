package eu.interedition.collatex2.interfaces;

import java.util.Set;

public interface ITokenIndex {

  // returns all the normalized phrases contained in this index
  Set<String> keys();
  
  IPhrase getPhrase(String normalized);

  boolean contains(String normalized);

  int size();

}
