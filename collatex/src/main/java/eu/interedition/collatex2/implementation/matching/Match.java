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

package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Match implements IMatch {

  private final IColumns a;
  private final IPhrase b;

  public Match(final IColumns columnsA, final IPhrase witnessPhrase) {
    this.a = columnsA;
    this.b = witnessPhrase;
  }

  @Override
  public String getNormalized() {
    return b.getNormalized();
  }

  @Override
  public IColumns getColumns() {
    return a;
  }

  @Override
  public IPhrase getPhrase() {
    return b;
  }

  @Override
  public String toString() {
    return getColumns() + "->" + getPhrase();
  }
}
