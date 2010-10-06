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

import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IReplacement;

public class Replacement implements IReplacement {
  private final IColumns _original;
  private final IPhrase _replacement;
  private final IInternalColumn _nextColumn;

  private Replacement(final IColumns gapA, final IPhrase replacement, final IInternalColumn nextColumn) {
    _original = gapA;
    _replacement = replacement;
    _nextColumn = nextColumn;
  }

  @Override
  public String toString() {
    final String baseWords = _original.toString();
    // TODO Not getNormalized!
    final String replacementWords = _replacement.getNormalized();
    return "replacement: " + baseWords + " / " + replacementWords + " position: " + _original.getFirstColumn().getPosition();
  }

  public int getPosition() {
    return _original.getFirstColumn().getPosition();
  }

  public IColumns getOriginalColumns() {
    return _original;
  }

  public IPhrase getReplacementPhrase() {
    return _replacement;
  }

  //TODO: do we need to make this defensive?
  @Override
  public IInternalColumn getNextColumn() {
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
