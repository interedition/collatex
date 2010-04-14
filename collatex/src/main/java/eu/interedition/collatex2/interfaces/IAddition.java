package eu.interedition.collatex2.interfaces;

public interface IAddition extends IModification {
  IPhrase getAddedPhrase();

  IColumn getNextColumn();

  boolean isAtTheBeginning();

  boolean isAtTheEnd();
  
}
