package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IAlignmentTable {

  List<String> getSigli();

  List<IColumn> getColumns();

  void add(IColumn column);

}
