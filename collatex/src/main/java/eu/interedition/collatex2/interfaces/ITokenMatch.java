package eu.interedition.collatex2.interfaces;

public interface ITokenMatch {

  String getNormalized();
  
  //TODO: notice the duplication here: choose one or the other!
  INormalizedToken getTableToken();

  INormalizedToken getWitnessToken();
  
  INormalizedToken getTokenA();

  INormalizedToken getTokenB();

}
