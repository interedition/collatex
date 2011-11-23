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

package eu.interedition.collatex2.implementation.vg_analysis;

import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;

import java.util.List;

public class Sequence implements ISequence {

  private final List<INormalizedToken> basePhrase;
  private final List<INormalizedToken> witnessPhrase;

  public Sequence(List<INormalizedToken> basePhrase, List<INormalizedToken> witnessPhrase) {
    this.basePhrase = basePhrase;
    this.witnessPhrase = witnessPhrase;
  }

  @Override
  public List<INormalizedToken> getBasePhrase() {
    return basePhrase;
  }

  @Override
  public List<INormalizedToken> getWitnessPhrase() {
    return witnessPhrase;
  }

  @Override
  public String getNormalized() {
    return NormalizedToken.toString(witnessPhrase);
  }

  @Override
  public String toString() {
    return NormalizedToken.toString(basePhrase);
  }
}
