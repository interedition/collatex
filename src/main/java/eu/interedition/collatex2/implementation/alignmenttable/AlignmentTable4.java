package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;

public class AlignmentTable4 implements IAlignmentTable {
  private final List<String> sigli;
  private final List<IColumn> columns;

  public AlignmentTable4() {
    this.sigli = Lists.newArrayList();
    this.columns = Lists.newArrayList();
  }

  @Override
  public void add(final IColumn column) {
    columns.add(column);
  }

  @Override
  public List<String> getSigli() {
    return sigli;
  }

  public List<IColumn> getColumns() {
    return columns;
  }

  @Override
  public String toString() {
    String collectedStrings = "";
    for (final String sigil : getSigli()) {
      collectedStrings += sigil + ": ";
      String delim = "";
      for (final IColumn column : getColumns()) {
        collectedStrings += delim + cellToString(sigil, column);
        delim = "|";
      }
      collectedStrings += "\n";
    }
    return collectedStrings;
  }

  private String cellToString(final String sigil, final IColumn column) {
    if (!column.containsWitness(sigil)) {
      return " ";
    }//TODO: should not be getnormalized!
    return column.getToken(sigil).getNormalized().toString();
  }

}
