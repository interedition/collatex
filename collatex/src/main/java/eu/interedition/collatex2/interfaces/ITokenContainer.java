package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface ITokenContainer {

  ITokenIndex getTokenIndex(List<String> repeatedTokens);

  //TODO: remove!
  List<String> getRepeatedTokens();
  
  boolean isNear(IToken a, IToken b);

}
