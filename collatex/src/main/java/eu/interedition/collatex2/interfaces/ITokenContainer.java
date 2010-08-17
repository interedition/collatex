package eu.interedition.collatex2.interfaces;

import java.util.Collection;
import java.util.List;


public interface ITokenContainer {

  Collection<? extends String> findRepeatingTokens();

  IWitnessIndex getTokenIndex(List<String> repeatingTokens);

}
