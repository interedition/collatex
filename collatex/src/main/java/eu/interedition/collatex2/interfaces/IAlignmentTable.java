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
 * A table consisting of rows as witnesses and columns containing aligned tokens
 * 
 */
public interface IAlignmentTable extends ITokenContainer {

  List<String> getSigla();

  List<IColumn> getColumns();

  void add(IColumn column);

  IColumns createColumns(int startIndex, int endIndex);

  int size();

  void addReplacement(IReplacement replacement);

  void addAddition(IAddition addition);

  //TODO: remove this method!
  //TODO: think about python bindings!
  List<String> findRepeatingTokens();

  void accept(IAlignmentTableVisitor visitor);

  IRow getRow(String sigil);
  
  /**
   * Retrieve the alignment table rows.
   * Each row represents a single witness.
   * 
   * @return alignment table rows
   */
  List<IRow> getRows();

}
