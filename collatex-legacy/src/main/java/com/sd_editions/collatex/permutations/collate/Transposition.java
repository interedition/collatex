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

package com.sd_editions.collatex.permutations.collate;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex.input.BaseElement;

//TODO make generic!
public class Transposition extends Modification {
  private final MatchSequence base;
  private final MatchSequence witness;
  private final Match<BaseElement> _nextMatch;

  public Transposition(final MatchSequence _base, final MatchSequence _witness, final Match nextMatch) {
    this.base = _base;
    this.witness = _witness;
    this._nextMatch = nextMatch;
  }

  @Override
  public String toString() {
    return "transposition: " + getLeft() + " switches position with " + getRight();
  }

  public String getLeft() {
    return base.baseToString();
  }

  public String getRight() {
    return witness.baseToString();
  }

  public MatchSequence getBase() {
    return base;
  }

  public MatchSequence getWitness() {
    return witness;
  }

  public Match getNextMatch() {
    return _nextMatch;
  }
}
