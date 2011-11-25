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

import java.util.Iterator;
import java.util.List;

/**
 *
 * A row of an alignment table which represents a single witness
 *
 *
 * TODO: consider whether this should be an inner interface since an IRow must exist within the context of an IAlignmentTable so the rows and columns will probably end up in the alignment table.
 *
 */
public class Row implements Iterable<Cell> {
  private List<Cell> cells;
  private final String sigil;

  public Row(String sigil, List<Cell> cells) {
    this.sigil = sigil;
    this.cells = cells;
  }

  @Override
  public Iterator<Cell> iterator() {
    return cells.iterator();
  }

  /**
   * get the witness sigil
   *
   * This identifies a witness
   *
   * TODO: should this be uniquely enforce within a collection of witnesses
   *
   * @return the witness sigil
   */
  public String getSigil() {
    return sigil;
  }
  
  @Override
  public String toString() {
    StringBuffer resultRow = new StringBuffer();
    resultRow.append(getSigil());
    resultRow.append(": ");
    for (Cell cell : this) {
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
