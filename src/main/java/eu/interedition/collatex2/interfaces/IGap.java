package eu.interedition.collatex2.interfaces;

public interface IGap {

  boolean isEmpty();

  boolean isAddition();

  boolean isReplacement();

  IColumns getColumnsA();

  IPhrase getPhraseB();

  //TODO: remove this method!
  //Modification should know about Gap, not the other way around!
  IModification getModification();

}
