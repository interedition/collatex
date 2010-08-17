package eu.interedition.collatex2.interfaces;

import java.util.List;


public interface IAlignmentTable extends ITokenContainer {

  List<String> getSigli();

  List<IColumn> getColumns();

  void add(IColumn column);

  IColumns createColumns(int startIndex, int endIndex);

  int size();

  void addReplacement(IReplacement replacement);

  void addAddition(IAddition addition);

  void accept(IAlignmentTableVisitor visitor);

  IRow getRow(IWitness witness);
  
  IRow getRow(String sigil);
  
  List<IRow> getRows();

  boolean isEmpty();

}
