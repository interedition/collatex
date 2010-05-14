package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IRow;

public class BaseAlignmentTable {
  protected final List<String> sigli;
  protected final List<IColumn> columns;

  public BaseAlignmentTable() {
    this.sigli = Lists.newArrayList();
    this.columns = Lists.newArrayList();
  }
  
  public List<IRow> getRows() {
    List<IRow> rows = Lists.newArrayList();
    for (String sigil: sigli) {
      rows.add(getRow(sigil));
    }
    return rows;
  }

  public IRow getRow(String sigil) {
    List<ICell> cells = Lists.newArrayList();
    for (IColumn column : columns) {
      ICell cell = new Cell(column, sigil);
      cells.add(cell);
    }
    return new Row(sigil, cells);
  }

}
