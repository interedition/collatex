package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IPhrase extends Comparable<IPhrase> {

  String getNormalized();

  int getBeginPosition();

  int getEndPosition();

  boolean isEmpty();

  INormalizedToken getFirstToken();

  String getSigil();

  List<INormalizedToken> getTokens();

  int size();

  IPhrase createSubPhrase(int startIndex, int endIndex);

  void addTokenToLeft(INormalizedToken leftToken);

  void addTokenToRight(INormalizedToken rightToken);

}
