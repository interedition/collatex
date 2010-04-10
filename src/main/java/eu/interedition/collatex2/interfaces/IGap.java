package eu.interedition.collatex2.interfaces;

public interface IGap {

  boolean isEmpty();

  boolean isAddition();

  boolean isOmission();

  boolean isReplacement();

  //TODO: rename this method! (remove A)
  IColumns getColumnsA();

  //TODO: rename this method! (remove B)
  IPhrase getPhraseB();

  //TODO remove this method!
  //Modification should know about Gap, not the other way around!
  IModification getModification();

}
