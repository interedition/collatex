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

package eu.interedition.collatex2.implementation.output;

import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IRow;

public class Row implements IRow {
  private List<ICell> cells;
  private final String sigil;

  public Row(String sigil, List<ICell> cells) {
    this.sigil = sigil;
    this.cells = cells;
  }

  @Override
  public Iterator<ICell> iterator() {
    return cells.iterator();
  }

  @Override
  public String getSigil() {
    return sigil;
  }
  
  @Override
  public String toString() {
    StringBuffer resultRow = new StringBuffer();
    resultRow.append(getSigil());
    resultRow.append(": ");
    for (ICell cell : this) {
      resultRow.append("|");
      if (cell.isEmpty()) {
        resultRow.append(" ");
      } else { 
        resultRow.append(cell.getToken().getContent());
      }  
    }
    resultRow.append("|");
    return resultRow.toString();
  }

}
