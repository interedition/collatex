package eu.interedition.collatex2.interfaces;

public interface ITokenMatch {

  INormalizedToken getBaseToken();

  INormalizedToken getWitnessToken();

  String getNormalized();
  
  //TODO: notice the duplication here: choose one or the other!
  INormalizedToken getTableToken();

  INormalizedToken getTokenA();

  INormalizedToken getTokenB();

}
