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

package eu.interedition.collatex2.legacy.gapdetection;

import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IColumns;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IGap;

public class Gap implements IGap {
  private final IColumns columns;
  private final IPhrase phrase;
  private final IColumn nextColumn;
  private final IWitness witness;

  public Gap(IWitness witness, final IColumns columns, final IPhrase phrase, final IColumn nextColumn) {
    this.columns = columns;
    this.phrase = phrase;
    this.nextColumn = nextColumn;
    this.witness = witness;
  }

  @Override
  public String toString() {
    if (isAddition()) {
      return "\"" + phrase.getNormalized() + "\" added";
    }
    if (isOmission()) {
      return columns.toString() + " omitted";
    }
    return columns.toString() + " -> " + witness.getSigil() + ": " + phrase.getNormalized();
  }

  @Override
  public IColumns getColumns() {
    return columns;
  }

  @Override
  public IPhrase getPhrase() {
    return phrase;
  }

  @Override
  public boolean isEmpty() {
    return columns.isEmpty() && phrase.isEmpty();
  }

  @Override
  public boolean isReplacement() {
    return !columns.isEmpty() && !phrase.isEmpty();
  }

  @Override
  public boolean isAddition() {
    return columns.isEmpty() && !phrase.isEmpty();
  }

  @Override
  public boolean isOmission() {
    return !columns.isEmpty() && phrase.isEmpty();
  }

  @Override
  public IColumn getNextColumn() {
    return nextColumn;
  }
}
