package eu.interedition.collatex2.interfaces;

import java.util.List;


public interface ITokenContainer {

  List<String> findRepeatingTokens();

  IWitnessIndex getTokenIndex(List<String> repeatingTokens);

}
