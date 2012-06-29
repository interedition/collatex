package eu.interedition.collatex.lab;

import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.matrix.Archipelago;
import eu.interedition.collatex.dekker.matrix.ArchipelagoWithVersions;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.simple.SimpleToken;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author Bram Buitendijk
 * @author Ronald Haentjens Dekker
 */
@SuppressWarnings("serial")
public class MatchMatrixTableModel extends AbstractTableModel {

  Logger LOG = org.slf4j.LoggerFactory.getLogger(MatchMatrixTableModel.class);
  private final String[] rowNames;
  private final String[] columnNames;
  private final MatchTableCell[][] data;

  public MatchMatrixTableModel(MatchTable matchTable, VariantGraph vg, Iterable<Token> witness) {
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
    //LOG.info(matchMatrix.toHtml(preferred));
    data = new MatchTableCell[rowNum][colNum];
    for (int row = 0; row < rowNum; row++) {
      for (int col = 0; col < colNum; col++) {
        VariantGraphVertex at = matchTable.at(row, col);
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
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (Island isl : islands) {
      archipelago.add(isl);
    }
    // find preferred islands
    Archipelago preferred = archipelago.createNonConflictingVersion();
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
