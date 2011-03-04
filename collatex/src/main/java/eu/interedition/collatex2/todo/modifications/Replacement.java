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

import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IColumns;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IGap;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IReplacement;

public class Replacement implements IReplacement {
  private final IColumns _original;
  private final IPhrase _replacement;
  private final IColumn _nextColumn;

  private Replacement(final IColumns gapA, final IPhrase replacement, final IColumn nextColumn) {
    _original = gapA;
    _replacement = replacement;
    _nextColumn = nextColumn;
  }

  @Override
  public String toString() {
    final String baseWords = _original.toString();
    // TODO Not getNormalized!
    final String replacementWords = _replacement.getNormalized();
    return "replacement: " + baseWords + " / " + replacementWords + " position: " + getPosition();
  }

  @Override
  public int getPosition() {
    return -1;
  }

  @Override
  public IColumns getOriginalColumns() {
    return _original;
  }

  @Override
  public IPhrase getReplacementPhrase() {
    return _replacement;
  }

  //TODO: do we need to make this defensive?
  @Override
  public IColumn getNextColumn() {
    return _nextColumn;
  }

  public static IReplacement create(IGap gap) {
    return new Replacement(gap.getColumns(), gap.getPhrase(), gap.getNextColumn());
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitReplacement(this);
  //  }

}
