package eu.interedition.collatex2.legacy.tokencontainers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.legacy.alignmenttable.Cell;
import eu.interedition.collatex2.legacy.alignmenttable.Column3;
import eu.interedition.collatex2.legacy.alignmenttable.Columns;
import eu.interedition.collatex2.legacy.alignmenttable.Row;
import eu.interedition.collatex2.todo.gapdetection.Gap;
import eu.interedition.collatex2.todo.modifications.Addition;

public class AlignmentTable4 implements IAlignmentTable {

  private final List<String> sigli;
  private final List<IInternalColumn> columns;

  public AlignmentTable4() {
    this.sigli = Lists.newArrayList();
    this.columns = Lists.newArrayList();
  }

  @Override
  public void add(final IInternalColumn column) {
    columns.add(column);
  }

  @Override
  public List<String> getSigla() {
    return sigli;
  }

  public List<IInternalColumn> getInternalColumns() {
    return columns;
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final String sigil : getSigla()) {
      stringBuilder.append(sigil).append(": ");
      String delim = "";
      for (final IInternalColumn column : getInternalColumns()) {
        stringBuilder.append(delim).append(cellToString(sigil, column));
        delim = "|";
      }
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  private String cellToString(final String sigil, final IInternalColumn column) {
    if (!column.containsWitness(sigil)) {
      return " ";
    }
    // TODO should not be getnormalized!
    return column.getToken(sigil).getNormalized().toString();
  }

  public static String alignmentTableToHTML(final IAlignmentTable alignmentTable) {
    final StringBuilder tableHTML = new StringBuilder(
        "<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table border=\"1\" class=\"alignment\">\n");
    for (final IRow row : alignmentTable.getRows()) {
      tableHTML.append("<tr>").append("<th>Witness ").append(row.getSigil()).append(":</th>");
      for (final ICell cell : row) {
        tableHTML.append("<td>");
        if (!cell.isEmpty()) {
          // TODO this was normalized!
          tableHTML.append(cell.getToken().getContent()); // TODO add escaping!
        }
        tableHTML.append("</td>");
      }
      tableHTML.append("</tr>\n");
    }
    tableHTML.append("</table>\n</div>\n\n");
    // return alignmentTable.toString().replaceAll("\n", "<br/>") + "<br/>";
    return tableHTML.toString();
  }

  private void addVariantAtTheEnd(final IPhrase witnessPhrase) {
    for (final INormalizedToken token : witnessPhrase.getTokens()) {
      final IInternalColumn extraColumn = new Column3(token, size() + 1);
      columns.add(extraColumn);
    }
  }

  // Note: this is a visitor that walks over the columns!
  @Override
  public void accept(final IAlignmentTableVisitor visitor) {
    visitor.visitTable(this);
    for (final IInternalColumn column : columns) {
      column.accept(visitor);
    }
    visitor.postVisitTable(this);
  }

  public void addVariantBefore(final IInternalColumn column, final IPhrase witnessPhrase) {
    int startPosition = column.getPosition();
    for (int i = startPosition; i <= columns.size(); i++) {
      final IInternalColumn mcolumn = columns.get(i - 1);
      final int position = mcolumn.getPosition();
      mcolumn.setPosition(position + witnessPhrase.size());
    }
    for (final INormalizedToken token : witnessPhrase.getTokens()) {
      final IInternalColumn extraColumn = new Column3(token, startPosition);
      columns.add(startPosition - 1, extraColumn);
      startPosition++;
    }
  }

  @Override
  public IColumns createColumns(final int startPosition, final int endPosition) {
    // NOTE: We make a new List here to prevent
    // ConcurrentModificationExceptions!
    final List<IInternalColumn> subList = Lists.newArrayList(columns.subList(startPosition - 1, endPosition));
    return new Columns(subList);
  }

  @Override
  public int size() {
    return getInternalColumns().size();
  }

  @Override
  public void addReplacement(final IReplacement replacement) {
    final IColumns originalColumns = replacement.getOriginalColumns();
    final IPhrase replacementPhrase = replacement.getReplacementPhrase();
    if (replacementPhrase.size() > originalColumns.size()) {
      Columns columns = new Columns();
      final IInternalColumn nextColumn = replacement.getNextColumn();
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

  @Override
  public void addAddition(final IAddition addition) {
    final IPhrase witnessPhrase = addition.getAddedPhrase();
    if (addition.isAtTheEnd()) {
      addVariantAtTheEnd(witnessPhrase);
    } else {
      final IInternalColumn column = addition.getNextColumn();
      addVariantBefore(column, witnessPhrase);
    }
  }

  @Override
  public List<String> getRepeatedTokens() {
    // transform
    final Multimap<String, IInternalColumn> columnsForTokenMap = ArrayListMultimap.create();
    for (final IInternalColumn column : getInternalColumns()) {
      final List<INormalizedToken> variants = column.getVariants();
      for (final INormalizedToken token : variants) {
        columnsForTokenMap.put(token.getNormalized(), column);
      }
    }
    // predicate
    final List<String> repeatingNormalizedTokens = Lists.newArrayList();
    for (final String tokenName : columnsForTokenMap.keySet()) {
      final Collection<IInternalColumn> columnCollection = columnsForTokenMap.get(tokenName);
      if (columnCollection.size() > 1) {
        // System.out.println("Repeating token: " + key + " in columns " +
        // xcolumns.toString());
        repeatingNormalizedTokens.add(tokenName);
      }
    }
    return repeatingNormalizedTokens;
  }

  @Override
  public IRow getRow(String sigil) {
    List<ICell> cells = Lists.newArrayList();
    for (IInternalColumn column : columns) {
      ICell cell = new Cell(column, sigil);
      cells.add(cell);
    }
    return new Row(sigil, cells);
  }

  @Override
  public ITokenIndex getTokenIndex(List<String> repeatingTokens) {
    return AlignmentTableIndex.create(this, repeatingTokens);
  }

  public IColumn getColumn(int position) {
    ArrayList<ICell> cells = Lists.newArrayList();
    IInternalColumn col = columns.get(position - 1);
    for (String sig : getSigla()) {
      ICell cell = new Cell(col, sig);
      cells.add(cell);
    }
    return new AlignmentTable4Column(position, cells, col);
  }

  @Override
  public List<IColumn> getColumns() {
    List<IColumn> cols = Lists.newArrayList();
    for (int i = 1; i <= columns.size(); ++i) {
      cols.add(getColumn(i));
    }
    return cols;
  }

  public class AlignmentTable4Column implements IColumn, Iterable<ICell> {

    private int position;
    private List<ICell> cells;
    private IInternalColumn internalColumn;

    // TODO: remove internalColumn exposure
    AlignmentTable4Column(int position, List<ICell> cells, IInternalColumn internalColumn) {
      this.internalColumn = internalColumn;
      this.position = position;
      this.cells = cells;
    }

    @Override
    public Iterator<ICell> iterator() {
      return cells.iterator();
    }

    @Override
    public int getPosition() {
      return position;
    }

    // TODO: hack remove
    @Override
    public IInternalColumn getInternalColumn() {
      return internalColumn;
    }

    @Override
    public int compareTo(IColumn o) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public boolean containsWitness(String sigil) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public INormalizedToken getToken(String sigil) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<INormalizedToken> getVariants() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void addVariant(INormalizedToken token) {
      // TODO Auto-generated method stub

    }

    @Override
    public void addMatch(INormalizedToken token) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setPosition(int position) {
      // TODO Auto-generated method stub

    }

    @Override
    public ColumnState getState() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<String> getSigli() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void accept(IAlignmentTableVisitor visitor) {
      // TODO Auto-generated method stub

    }

    @Override
    public void addVertex(IVariantGraphVertex vertex) {
      // TODO Auto-generated method stub

    }

  }

  @Override
  public IRow getRow(IWitness witness) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<IRow> getRows() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

}
