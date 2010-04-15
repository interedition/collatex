package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.interedition.collatex2.implementation.alignment.Gap;
import eu.interedition.collatex2.implementation.modifications.Addition;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.IRow;

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
    }
    //TODO should not be getnormalized!
    return column.getToken(sigil).getNormalized().toString();
  }

  public static String alignmentTableToHTML(final IAlignmentTable alignmentTable) {
    final StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table border=\"1\" class=\"alignment\">\n");

    for (final String witnessId : alignmentTable.getSigli()) {
      tableHTML.append("<tr>").//
          append("<th>Witness ").append(witnessId).append(":</th>");
      for (final IColumn column : alignmentTable.getColumns()) {
        tableHTML.append("<td>");
        if (column.containsWitness(witnessId)) {
          // TODO this was normalized!
          tableHTML.append(column.getToken(witnessId).getContent()); // TODO add escaping!
        }
        tableHTML.append("</td>");
      }
      tableHTML.append("</tr>\n");
    }
    tableHTML.append("</table>\n</div>\n\n");
    //    return alignmentTable.toString().replaceAll("\n", "<br/>") + "<br/>";
    return tableHTML.toString();
  }

  private void addVariantAtTheEnd(final IPhrase witnessPhrase) {
    for (final INormalizedToken token : witnessPhrase.getTokens()) {
      final IColumn extraColumn = new Column3(token, size() + 1);
      columns.add(extraColumn);
    }
  }

  // Note: this is a visitor that walks over the columns!
  public void accept(final IAlignmentTableVisitor visitor) {
    visitor.visitTable(this);
    for (final IColumn column : columns) {
      column.accept(visitor);
    }
    visitor.postVisitTable(this);
  }

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
    // NOTE: We make a new List here to prevent ConcurrentModificationExceptions!
    final List<IColumn> subList = Lists.newArrayList(columns.subList(startPosition - 1, endPosition));
    return new Columns(subList);
  }

  @Override
  public int size() {
    return getColumns().size();
  }

  public void addReplacement(final IReplacement replacement) {
    final IColumns originalColumns = replacement.getOriginalColumns();
    final IPhrase replacementPhrase = replacement.getReplacementPhrase();
    if (replacementPhrase.size() > originalColumns.size()) {
      Columns columns = new Columns();
      final IColumn nextColumn = replacement.getNextColumn();
      final IPhrase additionalPhrase = replacementPhrase.createSubPhrase(originalColumns.size() + 1, replacementPhrase.size());
      Gap gap = new Gap(columns, additionalPhrase, nextColumn);
      final IAddition addition = Addition.create(gap);
      final IPhrase smallerPhrase = replacementPhrase.createSubPhrase(1, originalColumns.size());
      originalColumns.addVariantPhrase(smallerPhrase);
      addAddition(addition);
    } else {
      originalColumns.addVariantPhrase(replacementPhrase);
    }
  }

  public void addAddition(final IAddition addition) {
    final IPhrase witnessPhrase = addition.getAddedPhrase();
    if (addition.isAtTheEnd()) {
      addVariantAtTheEnd(witnessPhrase);
    } else {
      final IColumn column = addition.getNextColumn();
      addVariantBefore(column, witnessPhrase);
    }
  }

  @Override
  public List<String> findRepeatingTokens() {
    //transform
    final Multimap<String, IColumn> columnsForTokenMap = Multimaps.newArrayListMultimap();
    for (final IColumn column : getColumns()) {
      final List<INormalizedToken> variants = column.getVariants();
      for (final INormalizedToken token : variants) {
        columnsForTokenMap.put(token.getNormalized(), column);
      }
    }
    //predicate
    final List<String> repeatingNormalizedTokens = Lists.newArrayList();
    for (final String tokenName : columnsForTokenMap.keySet()) {
      final Collection<IColumn> columnCollection = columnsForTokenMap.get(tokenName);
      if (columnCollection.size() > 1) {
        //System.out.println("Repeating token: " + key + " in columns " + xcolumns.toString());
        repeatingNormalizedTokens.add(tokenName);
      }
    }
    return repeatingNormalizedTokens;
  }

  @Override
  public IRow getRow(String sigil) {
    List<ICell> cells = Lists.newArrayList();
    for (IColumn column : columns) {
      ICell cell = new Cell(column, sigil);
      cells.add(cell);
    }
    return new Row(sigil, cells);
  }

  @Override
  public List<IRow> getRows() {
    List<IRow> rows = Lists.newArrayList();
    for (String sigil: sigli) {
      rows.add(getRow(sigil));
    }
    return rows;
  }
}
