package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IColumns {

  void addMatchPhrase(IPhrase phrase);

  void addVariantPhrase(IPhrase phrase);

  int getBeginPosition();

  int getEndPosition();

  IColumn getFirstColumn();

  boolean isEmpty();

  int size();

  //Note: exposes internal list; implement Collection instead?
  List<IColumn> getColumns();

}
