package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IPhrase {

  String getNormalized();

  int getBeginPosition();

  int getEndPosition();

  boolean isEmpty();

  INormalizedToken getFirstToken();

  String getSigil();

  List<INormalizedToken> getTokens();

  int size();

}
