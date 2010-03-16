package eu.interedition.collatex2.interfaces;

public interface IPhrase {

  String getNormalized();

  int getBeginPosition();

  int getEndPosition();

  boolean isEmpty();

  INormalizedToken getFirstToken();

  String getSigil();

}
