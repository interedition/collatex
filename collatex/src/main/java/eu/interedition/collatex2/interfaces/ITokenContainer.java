package eu.interedition.collatex2.interfaces;

import java.util.List;


public interface ITokenContainer {

  List<String> getRepeatedTokens();

  ITokenIndex getTokenIndex(List<String> repeatedTokens);

}
