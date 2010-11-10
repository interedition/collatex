package eu.interedition.collatex2.implementation.output.table;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: make class abstract
//TODO: make class implement the IAlignmentTable interface
//TODO: all the other methods are specific to the old implementation
public class BaseAlignmentTable {
  protected final List<String> sigla;
  protected final List<IColumn> columns;

  public BaseAlignmentTable() {
    this.sigla = Lists.newArrayList();
    this.columns = Lists.newArrayList();
  }
  
  public final List<IRow> getRows() {
    List<IRow> rows = Lists.newArrayList();
    for (String sigil: sigla) {
      rows.add(getRow(sigil));
    }
    return rows;
  }

  //TODO: REMOVE!
  public final IRow getRow(String sigil) {
    List<ICell> cells = Lists.newArrayList();
    for (IColumn column : columns) {
      ICell cell = new Cell(column.getInternalColumn(), sigil);
      cells.add(cell);
    }
    return new Row(sigil, cells);
  }

  public final boolean isEmpty() {
    return size()==0;
  }

  public final int size() {
    return getColumns().size();
  }

  public final List<IColumn> getColumns() {
    return columns;
  }

  public final List<String> getSigla() {
    return sigla;
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final IRow row : getRows()) {
      stringBuilder.append(row.getSigil()).append(": ");
      String delim = "";
      for (final ICell cell : row) {
        stringBuilder.append(delim).append(cellToString(cell));
        delim = "|";
      }
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  String cellToString(final ICell cell) {
    if (cell.isEmpty()) {
      return " ";
    }
    //TODO should not be getnormalized!
    return cell.getToken().getNormalized().toString();
  }

  public IRow getRow(IWitness witness) {
    return this.getRow(witness.getSigil());
  }

}
