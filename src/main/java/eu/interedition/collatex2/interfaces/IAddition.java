package eu.interedition.collatex2.interfaces;

public interface IAddition extends IModification {
  IPhrase getAddedWords();

  boolean isAtTheEnd();

  INormalizedToken getNextMatchToken();
}
