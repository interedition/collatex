package eu.interedition.collatex2.interfaces;


public interface INGram {

  String getNormalized();

  int getBeginPosition();

  int getEndPosition();

  boolean isEmpty();

  INormalizedToken getFirstToken();

}
