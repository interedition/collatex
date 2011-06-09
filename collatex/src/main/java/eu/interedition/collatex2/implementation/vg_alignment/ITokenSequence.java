package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.List;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public interface ITokenSequence {

  INormalizedToken getFirstToken();

  INormalizedToken getLastToken();

  String getNormalized();

  boolean expandsToTheRight();

  List<INormalizedToken> getTokens();
  
}
