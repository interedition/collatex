/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.lab;

import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.Archipelago;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.IslandConflictResolver;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.simple.SimpleToken;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author Bram Buitendijk
 * @author Ronald Haentjens Dekker
 */
@SuppressWarnings("serial")
public class MatchMatrixTableModel extends AbstractTableModel {

  private final String[] rowNames;
  private final String[] columnNames;
  private final MatchTableCell[][] data;
  private final int outlierTranspositionsSizeLimit;

  public MatchMatrixTableModel(MatchTable matchTable, VariantGraph vg, Iterable<Token> witness, int outlierTranspositionsSizeLimit) {
    this.outlierTranspositionsSizeLimit = outlierTranspositionsSizeLimit;
    List<Token> rowList = matchTable.rowList();
    List<Integer> columnList = matchTable.columnList();

    final int rowNum = rowList.size();
    final int colNum = columnList.size();

    // set the row labels
    rowNames = new String[rowNum];
    for (int row = 0; row < rowNum; row++) {
      rowNames[row] = ((SimpleToken) rowList.get(row)).getContent();
    }

    // set the column labels
    columnNames = new String[colNum];
    for (int col = 0; col < colNum; col++) {
      columnNames[col] = Integer.toString(columnList.get(col) + 1);
    }

    // fill the cells with colors
    Archipelago preferred = preferred(matchTable);
    //LOG.debug(matchMatrix.toHtml(preferred));
    data = new MatchTableCell[rowNum][colNum];
    for (int row = 0; row < rowNum; row++) {
      for (int col = 0; col < colNum; col++) {
        VariantGraph.Vertex at = matchTable.vertexAt(row, col);
        MatchMatrixCellStatus status;
        if (at != null) {
          status = preferred.containsCoordinate(row, col) ? MatchMatrixCellStatus.PREFERRED_MATCH : MatchMatrixCellStatus.OPTIONAL_MATCH;
        } else {
          status = MatchMatrixCellStatus.EMPTY;
        }
        String text;
        if (at != null) {
          text = ((SimpleToken) at.tokens().iterator().next()).getContent();
        } else {
          text = null;
        }
        data[row][col] = new MatchTableCell(status, text);
      }
    }
  }

  private Archipelago preferred(MatchTable matchTable) {
    // detect islands
    Set<Island> islands = matchTable.getIslands();
    // prepare
    IslandConflictResolver resolver = new IslandConflictResolver(matchTable, outlierTranspositionsSizeLimit);
    // find preferred islands
    Archipelago preferred = resolver.createNonConflictingVersion(islands);
    return preferred;
  }

  @Override
  public String getColumnName(int column) {
    return (column == 0 ? "" : columnNames[column - 1]);
  }

  @Override
  public int getRowCount() {
    return data.length;
  }

  @Override
  public int getColumnCount() {
    return (data.length == 0 ? 0 : data[0].length + 1);
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == 0) {
      return rowNames[rowIndex];
    }
    return data[rowIndex][columnIndex - 1];
  }
}
