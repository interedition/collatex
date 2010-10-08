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

import java.util.List;

/**
 * 
 * A row of an alignment table which represents a single witness
 * 
 * 
 * TODO: consider whether this should be an inner interface since an IRow must exist within the context of an IAlignmentTable so the rows and columns will probably end up in the alignment table.
 *
 */
public interface IColumn extends Comparable<IColumn> {

  boolean containsWitness(String sigil);

  INormalizedToken getToken(String sigil);

  List<INormalizedToken> getVariants();

  void addVariant(INormalizedToken token);

  void addMatch(INormalizedToken token);
	
  /**
   * get the position of this column within the alignment table
   * 
   * @return the position of this column
   */
  int getPosition();

  void setPosition(int position);

  ColumnState getState();

  List<String> getSigli();

  void accept(IAlignmentTableVisitor visitor);

  //TODO: remove add methods from interface!
  void addVertex(IVariantGraphVertex vertex);

  /**
   * get the internal representation of the alignment column
   * This is only intended for internal use
   * 
   * @return the internal alignment column
   */
  IInternalColumn getInternalColumn();
}
