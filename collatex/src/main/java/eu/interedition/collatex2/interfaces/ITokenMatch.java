package eu.interedition.collatex2.interfaces;

public interface ITokenMatch {

  String getNormalized();

  INormalizedToken getTableToken();

  INormalizedToken getWitnessToken();

}
