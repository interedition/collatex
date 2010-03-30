package eu.interedition.collatex2.interfaces;

public interface IAddition extends IModification {
  IPhrase getAddedPhrase();

  boolean isAtTheEnd();

  IColumn getNextColumn();
}
