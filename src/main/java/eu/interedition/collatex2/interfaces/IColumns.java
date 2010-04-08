package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IColumns extends Comparable<IColumns> {

  void addMatchPhrase(IPhrase phrase);

  void addVariantPhrase(IPhrase phrase);

  int getBeginPosition();

  int getEndPosition();

  IColumn getFirstColumn();

  boolean isEmpty();

  int size();

  List<IColumn> getColumns();

}
