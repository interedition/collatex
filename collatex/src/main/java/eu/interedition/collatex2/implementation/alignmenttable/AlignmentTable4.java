/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import eu.interedition.collatex2.implementation.alignment.Gap;
import eu.interedition.collatex2.implementation.indexing.AlignmentTableIndex;
import eu.interedition.collatex2.implementation.modifications.Addition;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.ITokenIndex;

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
    //TODO should not be getnormalized!
    return column.getToken(sigil).getNormalized().toString();
  }

  public static String alignmentTableToHTML(final IAlignmentTable alignmentTable) {
    final StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table border=\"1\" class=\"alignment\">\n");
    for (final IRow row : alignmentTable.getRows()) {
      tableHTML.append("<tr>").
          append("<th>Witness ").append(row.getSigil()).append(":</th>");
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
    //    return alignmentTable.toString().replaceAll("\n", "<br/>") + "<br/>";
    return tableHTML.toString();
  }

  private void addVariantAtTheEnd(final IPhrase witnessPhrase) {
    for (final INormalizedToken token : witnessPhrase.getTokens()) {
      final IInternalColumn extraColumn = new Column3(token, size() + 1);
      columns.add(extraColumn);
    }
  }

  // Note: this is a visitor that walks over the columns!
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
    // NOTE: We make a new List here to prevent ConcurrentModificationExceptions!
    final List<IInternalColumn> subList = Lists.newArrayList(columns.subList(startPosition - 1, endPosition));
    return new Columns(subList);
  }

  @Override
  public int size() {
    return getInternalColumns().size();
  }

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
  public List<String> findRepeatingTokens() {
    //transform
    final Multimap<String, IInternalColumn> columnsForTokenMap = ArrayListMultimap.create();
    for (final IInternalColumn column : getInternalColumns()) {
      final List<INormalizedToken> variants = column.getVariants();
      for (final INormalizedToken token : variants) {
        columnsForTokenMap.put(token.getNormalized(), column);
      }
    }
    //predicate
    final List<String> repeatingNormalizedTokens = Lists.newArrayList();
    for (final String tokenName : columnsForTokenMap.keySet()) {
      final Collection<IInternalColumn> columnCollection = columnsForTokenMap.get(tokenName);
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
    for (IInternalColumn column : columns) {
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

  public IColumn getColumn(int position) {
    ArrayList<ICell> cells = Lists.newArrayList();
    IInternalColumn col = columns.get(position-1);
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
  @Override
  public Collection<? extends String> getRepeatedTokens() {
    return findRepeatingTokens();
  }

  @Override
  public ITokenIndex getTokenIndex(List<String> repeatedTokens) {
    return AlignmentTableIndex.create(this, repeatedTokens);
  }

  public class AlignmentTable4Column implements IColumn {

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
	  
  }
  

}
