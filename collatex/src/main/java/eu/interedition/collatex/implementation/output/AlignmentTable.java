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

package eu.interedition.collatex.implementation.output;

import com.google.common.collect.Lists;
import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter;
import eu.interedition.collatex.implementation.graph.db.VariantGraph;
import eu.interedition.collatex.implementation.graph.db.VariantGraphVertex;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 *
 * A table consisting of rows as witnesses and columns containing aligned tokens
 *
 */
public class AlignmentTable {
  private final VariantGraph graph;
  protected final List<Column> columns = Lists.newArrayList();

  public AlignmentTable(VariantGraph graph) {
    this.graph = graph.rank();
    final Iterator<VariantGraphVertex> vertexIterator = graph.vertices().iterator();
    //skip startVertex
    vertexIterator.next();
    while(vertexIterator.hasNext()) {
      VariantGraphVertex next = vertexIterator.next();
      if (next.equals(graph.getEnd())) {
        continue;
      }
      final int rank = next.getRank();
      if (rank>columns.size()) {
        addNewColumn(next);
      } else {
        (columns.get(rank-1)).addVertex(next);
      }
    }
  }

  private Column addNewColumn(VariantGraphVertex vertex) {
    final Column column = new Column(vertex);
    columns.add(column);
    return column;
  }
  
  public final SortedSet<IWitness> getWitnesses() {
    return graph.witnesses();
  }

  /**
   * Retrieve the alignment table rows.
   * Each row represents a single witness.
   *
   * @return alignment table rows
   */
  public final List<Row> getRows() {
    List<Row> rows = Lists.newArrayList();
    for (IWitness witness: getWitnesses()) {
      rows.add(getRow(witness));
    }
    return rows;
  }

  public final Row getRow(IWitness witness) {
    List<Cell> cells = Lists.newArrayList();
    for (Column column : columns) {
      Cell cell = new Cell(column, witness);
      cells.add(cell);
    }
    return new Row(witness.getSigil(), cells);
  }

  public final boolean isEmpty() {
    return size()==0;
  }

  public final int size() {
    return getColumns().size();
  }

  public final List<Column> getColumns() {
    return columns;
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final Row row : getRows()) {
      stringBuilder.append(row.getSigil()).append(": ");
      String delim = "";
      for (final Cell cell : row) {
        stringBuilder.append(delim).append(cellToString(cell));
        delim = "|";
      }
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  String cellToString(final Cell cell) {
    if (cell.isEmpty()) {
      return " ";
    }
    //TODO should not be getnormalized!
    return ((SimpleToken) cell.getToken()).getNormalized().toString();
  }
}
