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

import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IVariantGraphVertex;
import eu.interedition.collatex.interfaces.IWitness;

/**
 *
 * An alignment table cell represents the position of a witness token in alignment position with other witnesses in the table.
 * Empty cells occur where this witness has nothing to represent at this position in the alignment.
 *
 */
public class Cell {
  private final Column column;
  private final IWitness witness;

  public Cell(Column column, IWitness witness) {
    this.column = column;
    this.witness = witness;
  }

  public Column getColumn() {
    return column;
  }

  private String color(int hashCode) {
    return "#" + (Integer.toHexString(hashCode) + "000000").substring(0, 6);
  }

  /**
   * Retrieve the token for this cell if present
   * @return the token for this cell
   */
  public INormalizedToken getToken() {
    return column.getToken(witness);
  }

  /**
   * It is important to call this method before attempting to call getToken to determine if a token is actually present.
   * Empty cells occur where this witness has nothing to represent at this position in the alignment.
   * @return whether or not this cell is empty
   */
  public boolean isEmpty() {
    return !column.containsWitness(witness);
  }
}
