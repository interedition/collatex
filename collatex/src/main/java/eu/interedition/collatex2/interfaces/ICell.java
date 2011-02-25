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

package eu.interedition.collatex2.interfaces;


/**
 * 
 * An alignment table cell represents the position of a witness token in alignment position with other witnesses in the table.
 * Empty cells occur where this witness has nothing to represent at this position in the alignment.
 * 
 */
public interface ICell {

  /**
   * It is important to call this method before attempting to call getToken to determine if a token is actually present.
   * Empty cells occur where this witness has nothing to represent at this position in the alignment.
   * @return whether or not this cell is empty
   */
  boolean isEmpty();

  /**
   * Retrieve the token for this cell if present
   * @see isEmpty()
   * @return the token for this cell
   */
  INormalizedToken getToken();

  /**
   * Get a color value for this cell
   * cells from the same vertex get the same color
   */
  String getColor();

  IColumn getColumn();

}
