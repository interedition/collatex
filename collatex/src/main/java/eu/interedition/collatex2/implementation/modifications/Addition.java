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

package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Addition implements IAddition {
  private final IPhrase addition;
  private final IColumn nextColumn;

  private Addition(final IColumn nextColumn, final IPhrase addition) {
    this.nextColumn = nextColumn;
    this.addition = addition;
  }

  public int getPosition() {
    return getNextColumn().getPosition();
  }

  public IPhrase getAddedPhrase() {
    return addition;
  }

  @Override
  public String toString() {
    // TODO should not be get Normalized?
    String result = "addition: " + addition.getNormalized();
    // TODO I would like to have only 
    if (isAtTheEnd()) {
      result += " position: at the end";
    } else {
      result += " position: " + getPosition();
    }
    return result;
  }

  @Override
  public boolean isAtTheBeginning() {
    return addition.getBeginPosition() == 1;
  }

  @Override
  public boolean isAtTheEnd() {
    return nextColumn == null;
  }

  @Override
  public IColumn getNextColumn() {
    if (isAtTheEnd()) {
      throw new RuntimeException("There is no next match!");
    }
    return nextColumn;
  }

  public static IAddition create(IGap gap) {
    return new Addition(gap.getNextColumn(), gap.getPhrase());
  }


  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitAddition(this);
  //  }

}
