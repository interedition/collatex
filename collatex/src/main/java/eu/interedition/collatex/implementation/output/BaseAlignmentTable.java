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
import eu.interedition.collatex.interfaces.ICell;
import eu.interedition.collatex.interfaces.IColumn;
import eu.interedition.collatex.interfaces.IRow;
import eu.interedition.collatex.interfaces.IWitness;

public abstract class BaseAlignmentTable implements IAlignmentTable {
  protected final List<IColumn> columns;

  public BaseAlignmentTable() {
    this.columns = Lists.newArrayList();
  }
  
  public final List<IRow> getRows() {
    List<IRow> rows = Lists.newArrayList();
    for (IWitness witness: getWitnesses()) {
      rows.add(getRow(witness));
    }
    return rows;
  }

  public final IRow getRow(IWitness witness) {
    List<ICell> cells = Lists.newArrayList();
    for (IColumn column : columns) {
      ICell cell = new Cell(column, witness);
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

  public final List<IColumn> getColumns() {
    return columns;
  }

  public abstract List<IWitness> getWitnesses();
  
  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final IRow row : getRows()) {
      stringBuilder.append(row.getSigil()).append(": ");
      String delim = "";
      for (final ICell cell : row) {
        stringBuilder.append(delim).append(cellToString(cell));
        delim = "|";
      }
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  String cellToString(final ICell cell) {
    if (cell.isEmpty()) {
      return " ";
    }
    //TODO should not be getnormalized!
    return cell.getToken().getNormalized().toString();
  }


}
