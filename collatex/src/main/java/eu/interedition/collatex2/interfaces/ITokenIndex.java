package eu.interedition.collatex2.interfaces;

import java.util.Set;

public interface ITokenIndex {

  IPhrase getPhrase(String key);

  boolean contains(String key);

  Set<String> keys();

}
