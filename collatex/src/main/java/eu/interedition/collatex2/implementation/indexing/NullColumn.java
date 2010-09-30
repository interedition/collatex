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

package eu.interedition.collatex2.implementation.indexing;

import java.util.List;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NullColumn implements IInternalColumn {

  private final int position;

  public NullColumn(final int position) {
    this.position = position;
  }

  @Override
  public void addMatch(final INormalizedToken token) {}

  @Override
  public void addVariant(final INormalizedToken token) {}

  @Override
  public boolean containsWitness(final String sigil) {
    return false;
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public INormalizedToken getToken(final String sigil) {
    return new NullToken(0, sigil);
  }

  @Override
  public List<INormalizedToken> getVariants() {
    return null;
  }

  @Override
  public void setPosition(final int position) {}

  @Override
  public ColumnState getState() {
    return null;
  }

  @Override
  public int compareTo(final IInternalColumn o) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<String> getSigla() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void accept(final IAlignmentTableVisitor visitor) {
  // TODO Auto-generated method stub

  }
  
  @Override
  public String toString() {
    return "";
  }

}
