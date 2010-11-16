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
// TODO: some cleanups went missing during the merge!
// TODO: The IColumn interface should contain less methods
// TODO: and have Iterable ICells (see CollateX branch 0.9.1)
// TODO: Do not expose getToken(sigil)!
// TODO: Do not expose addXXXX methods!
// TODO: Rename getSigli to getSigla()!
public interface IColumn {
  
  INormalizedToken getToken(String sigil);

  ColumnState getState();

  //TODO: rename to getSigla()!
  List<String> getSigli();

  //TODO: remove add methods from interface!
  void addVertex(IVariantGraphVertex vertex);

  /**
   * get the position of this column within the alignment table
   * 
   * @return the position of this column
   */
  //TODO: Warning: seems to be used only in tests!
  int getPosition();

  //TODO: Warning method is only used in test!
  boolean containsWitness(String sigil);

  //TODO: Warning method is only used in test!
  List<INormalizedToken> getVariants();

  //TODO: remove add method!
  void addVariant(INormalizedToken token);

  //TODO: remove add method!
  void addMatch(INormalizedToken token);
	
  //TODO: remove set method!
  void setPosition(int position);

  //TODO: not used anymore?
  void accept(IAlignmentTableVisitor visitor);

  /**
   * get the internal representation of the alignment column
   * This is only intended for internal use
   * 
   * @return the internal alignment column
   */
  IInternalColumn getInternalColumn();

}
