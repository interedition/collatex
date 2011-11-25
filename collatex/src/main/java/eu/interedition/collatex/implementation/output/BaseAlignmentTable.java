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

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.interfaces.IAlignmentTable;
import eu.interedition.collatex.interfaces.IWitness;

public abstract class BaseAlignmentTable implements IAlignmentTable {
  protected final List<Column> columns;

  public BaseAlignmentTable() {
    this.columns = Lists.newArrayList();
  }

  @Override
  public final List<Row> getRows() {
    List<Row> rows = Lists.newArrayList();
    for (IWitness witness : getWitnesses()) {
      rows.add(getRow(witness));
    }
    return rows;
  }

  @Override
  public final Row getRow(IWitness witness) {
    List<Cell> cells = Lists.newArrayList();
    for (Column column : columns) {
      Cell cell = new Cell(column, witness);
      cells.add(cell);
    }
    return new Row(witness.getSigil(), cells);
  }

  @Override
  public final boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public final int size() {
    return getColumns().size();
  }

  @Override
  public final List<Column> getColumns() {
    return columns;
  }

  @Override
  public abstract List<IWitness> getWitnesses();

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
    return cell.getToken().getNormalized().toString();
  }

}
