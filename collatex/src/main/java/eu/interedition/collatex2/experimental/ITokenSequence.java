package eu.interedition.collatex2.experimental;

import java.util.List;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public interface ITokenSequence {

  INormalizedToken getFirstToken();

  INormalizedToken getLastToken();

  String getNormalized();

  boolean isLeftAligned();

  List<INormalizedToken> getTokens();
  
}
