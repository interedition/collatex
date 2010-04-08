package eu.interedition.collatex2.interfaces;

public interface IColumns extends Comparable<IColumns> {

  void addMatchPhrase(IPhrase phrase);

  void addVariantPhrase(IPhrase phrase);

  int getBeginPosition();

  int getEndPosition();

  IColumn getFirstColumn();

  boolean isEmpty();

  int size();

}
