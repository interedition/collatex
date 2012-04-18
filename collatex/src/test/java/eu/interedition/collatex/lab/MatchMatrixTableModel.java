package eu.interedition.collatex.lab;

import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import com.google.common.collect.Iterables;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.MatrixLinker.MatchMatrix;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.simple.SimpleToken;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@SuppressWarnings("serial")
public class MatchMatrixTableModel extends AbstractTableModel {
  private final String[] rowNames;
  private final String[] columnNames;
  private final Status[][] data;

  public MatchMatrixTableModel(MatchMatrix matchMatrix, VariantGraph vg, Iterable<Token> witness) {
    final int rowNum = matchMatrix.rowNum();
    final int colNum = matchMatrix.colNum();

    final Iterator<VariantGraphVertex> vertexIt = vg.vertices().iterator();
    vertexIt.next(); // skip start vertex
    rowNames = new String[rowNum];
    for (int row = 0; row < rowNum; row++) {
      rowNames[row] = ((SimpleToken) Iterables.getFirst(vertexIt.next().tokens(), null)).getContent();
    }

    columnNames = new String[colNum];
    final Iterator<Token> witnessIt = witness.iterator();
    for (int col = 0; col < colNum; col++) {
      columnNames[col] = ((SimpleToken) witnessIt.next()).getContent();
    }

    data = new Status[rowNum][colNum];
    for (int row = 0; row < rowNum; row++) {
      for (int col = 0; col < colNum; col++) {
        data[row][col] = matchMatrix.at(row, col);
      }
    }
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
