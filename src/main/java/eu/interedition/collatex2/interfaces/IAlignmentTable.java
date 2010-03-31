package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IAlignmentTable {

  List<String> getSigli();

  List<IColumn> getColumns();

  void add(IColumn column);

  void addVariantAtTheEnd(IPhrase witnessPhrase);

  void addVariantBefore(IColumn column, IPhrase witnessPhrase);

  //TODO Maybe rename to getCells?
  IColumns createColumns(int currentIndex, int i);

  int size();

}
