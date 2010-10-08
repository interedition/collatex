package eu.interedition.collatex2.interfaces;

import java.util.Collection;
import java.util.List;

public interface ITokenContainer {

  ITokenIndex getTokenIndex(List<String> repeatedTokens);

  //TODO: remove!
  Collection<? extends String> getRepeatedTokens();

}
