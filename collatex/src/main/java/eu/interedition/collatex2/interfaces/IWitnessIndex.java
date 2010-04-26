package eu.interedition.collatex2.interfaces;

import java.util.Collection;

public interface IWitnessIndex {

  boolean contains(String normalized);

  int size();

  Collection<IPhrase> getPhrases();

  IPhrase getPhrase(String normalized);

}
