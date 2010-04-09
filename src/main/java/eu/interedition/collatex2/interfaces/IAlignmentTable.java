package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IAlignmentTable {

  List<String> getSigli();

  List<IColumn> getColumns();

  void add(IColumn column);

  //TODO: Maybe rename to getCells?
  IColumns createColumns(int currentIndex, int i);

  int size();

  void addReplacement(IReplacement replacement);

  void addAddition(IAddition addition);

  List<String> findRepeatingTokens();

}
