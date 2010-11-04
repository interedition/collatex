package eu.interedition.collatex2.interfaces;

import java.util.Iterator;
import java.util.List;

public interface ITokenContainer {

  ITokenIndex getTokenIndex(List<String> repeatedTokens);

  //TODO: remove! (when AlternativeTokenIndexMatcher is used!)
  List<String> getRepeatedTokens();
  
  boolean isNear(IToken a, IToken b);

  Iterator<INormalizedToken> tokenIterator();

}
