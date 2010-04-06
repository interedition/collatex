package eu.interedition.collatex2.interfaces;

public interface IMatch {

  String getNormalized();

  //TODO: rename to getColumns!
  IColumns getColumnsA();

  //TODO: rename to getPhrase!
  IPhrase getPhraseB();

}
