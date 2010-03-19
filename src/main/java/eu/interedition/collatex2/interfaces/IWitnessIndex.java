package eu.interedition.collatex2.interfaces;

import java.util.Collection;

public interface IWitnessIndex {

  boolean contains(String string);

  int size();

  Collection<IPhrase> getPhrases();

}
