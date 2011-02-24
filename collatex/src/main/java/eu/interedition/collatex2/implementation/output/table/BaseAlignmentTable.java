package eu.interedition.collatex2.implementation.output.table;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.IWitness;

public abstract class BaseAlignmentTable implements IAlignmentTable {
  protected final List<IColumn> columns;

  public BaseAlignmentTable() {
    this.columns = Lists.newArrayList();
  }
  
  public final List<IRow> getRows() {
    List<IRow> rows = Lists.newArrayList();
    for (IWitness witness: getWitnesses()) {
      rows.add(getRow(witness));
    }
    return rows;
  }

  public final IRow getRow(IWitness witness) {
    List<ICell> cells = Lists.newArrayList();
    for (IColumn column : columns) {
      ICell cell = new Cell(column.getInternalColumn(), witness);
      cells.add(cell);
    }
    return new Row(witness.getSigil(), cells);
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

  public abstract List<IWitness> getWitnesses();
  
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


}
