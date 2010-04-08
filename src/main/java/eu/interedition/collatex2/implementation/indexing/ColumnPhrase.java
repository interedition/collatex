package eu.interedition.collatex2.implementation.indexing;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;

public class ColumnPhrase {
  // IColumns columns is a consecutive list of IColumn-s
  // sigli is a list of witness siglis, such that for each IColumn in columns, there is a token in that column for all of the sigli, and all theses tokens match. 
  private IColumns columns;
  private List<String> sigli;
  String name;

  public ColumnPhrase(final String _name, final IColumns _columns, final Collection<String> _sigli) {
    this.setColumns(_columns);
    this.setSigli(_sigli);
    this.name = _name;
  }

  public void addColumnToLeft(final IColumn column) {
    if (column instanceof NullColumn) {
      name = new StringBuilder("+ ").append(name).toString();
    } else {
      final List<IColumn> columnList = getColumns().getColumns();
      columnList.add(0, column);
      setColumns(new Columns(columnList));
      final String normalized = column.getToken(getSigli().get(0)).getNormalized();
      name = new StringBuilder(normalized).append(" ").append(name).toString();
    }
  }

  public void addColumnToRight(final IColumn column) {
    if (column instanceof NullColumn) {
      name = new StringBuilder(name).append(" +").toString();
    } else {
      final List<IColumn> columnList = getColumns().getColumns();
      columnList.add(column);
      setColumns(new Columns(columnList));
      final String normalized = column.getToken(getSigli().get(0)).getNormalized();
      name = new StringBuilder(name).append(" ").append(normalized).toString();
    }
  }

  public String getNormalized() {
    return name;
  }

  public void setSigli(final Collection<String> sigli1) {
    this.sigli = Lists.newArrayList(sigli1);
  }

  public List<String> getSigli() {
    return sigli;
  }

  public void setColumns(final IColumns columns1) {
    this.columns = columns1;
  }

  public IColumns getColumns() {
    return columns;
  }
}
