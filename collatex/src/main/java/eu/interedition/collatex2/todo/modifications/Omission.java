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

package eu.interedition.collatex2.todo.modifications;

import eu.interedition.collatex2.interfaces.nonpublic.modifications.IColumns;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IGap;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IOmission;

public class Omission implements IOmission {
  private final IColumns columns;

  private Omission(final IColumns gapA) {
    this.columns = gapA;
  }

  @Override
  public IColumns getOmittedColumns() {
    return columns;
  }

  @Override
  public int getPosition() {
    return -1;
  }

  //TODO should not be getNormalized!
  @Override
  public String toString() {
    return "omission: " + columns.toString() + " position: " + getPosition();
  }

  public static IOmission create(IGap gap) {
    return new Omission(gap.getColumns());
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitOmission(this);
  //  }
}
