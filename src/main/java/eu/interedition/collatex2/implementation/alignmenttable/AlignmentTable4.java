package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

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
    final StringBuilder stringBuilder = new StringBuilder();
    for (final String sigil : getSigli()) {
      stringBuilder.append(sigil).append(": ");
      String delim = "";
      for (final IColumn column : getColumns()) {
        stringBuilder.append(delim).append(cellToString(sigil, column));
        delim = "|";
      }
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  private String cellToString(final String sigil, final IColumn column) {
    if (!column.containsWitness(sigil)) {
      return " ";
    }//TODO: should not be getnormalized!
    return column.getToken(sigil).getNormalized().toString();
  }

  public static String alignmentTableToHTML(final IAlignmentTable alignmentTable) {
    final StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table border=\"1\" class=\"alignment\">\n");

    for (final String witnessId : alignmentTable.getSigli()) {
      tableHTML.append("<tr>");
      tableHTML.append("<th>Witness ").append(witnessId).append(":</th>");
      for (final IColumn column : alignmentTable.getColumns()) {
        tableHTML.append("<td>");
        if (column.containsWitness(witnessId)) {
          // TODO: this was normalized!
          tableHTML.append(column.getToken(witnessId).getContent()); // TODO: add escaping!
        }
        tableHTML.append("</td>");
      }
      tableHTML.append("</tr>\n");
    }
    tableHTML.append("</table>\n</div>\n\n");
    //    return alignmentTable.toString().replaceAll("\n", "<br/>") + "<br/>";
    return tableHTML.toString();
  }

  @Override
  public void addVariantAtTheEnd(final IPhrase witnessPhrase) {
    for (final INormalizedToken token : witnessPhrase.getTokens()) {
      final IColumn extraColumn = new Column3(token, size() + 1);
      columns.add(extraColumn);
    }
  }

  @Override
  public void addVariantBefore(final IColumn column, final IPhrase witnessPhrase) {
    int startPosition = column.getPosition();
    for (int i = startPosition; i <= columns.size(); i++) {
      final IColumn mcolumn = columns.get(i - 1);
      final int position = mcolumn.getPosition();
      mcolumn.setPosition(position + witnessPhrase.size());
    }
    for (final INormalizedToken token : witnessPhrase.getTokens()) {
      final IColumn extraColumn = new Column3(token, startPosition);
      columns.add(startPosition - 1, extraColumn);
      startPosition++;
    }
  }

  @Override
  public IColumns createColumns(final int startPosition, final int endPosition) {
    final List<IColumn> subList = columns.subList(startPosition - 1, endPosition);
    return new Columns(subList);
  }

  @Override
  public int size() {
    return getColumns().size();
  }
}
