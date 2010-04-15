package eu.interedition.collatex2.interfaces;

public interface IGap {

  boolean isEmpty();

  boolean isAddition();

  boolean isOmission();

  boolean isReplacement();

  IColumns getColumns();

  IPhrase getPhrase();

  IColumn getNextColumn();

}
