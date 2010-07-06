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

package com.sd_editions.collatex.permutations;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Word;

public class PMatch {
  final Match<Word> match;
  private boolean fixed;

  public PMatch(Match _match) {
    match = _match;
    fixed = false;
  }

  public Word getBaseWord() {
    return match.getBaseWord();
  }

  public Word getWitnessWord() {
    return match.getWitnessWord();
  }

  public void fix() {
    this.fixed = true;
  }

  public boolean isFixed() {
    return fixed;
  }

  @Override
  public String toString() {
    return "[" + getBaseWord().position + "->" + getWitnessWord().position + "]" + (this.isFixed() ? " (fixed)" : " (potential)");
  }

  public float getLevDistance() {
    return match.wordDistance;
  }

  public PMatch copy() {
    PMatch copy = new PMatch(this.match);
    if (this.isFixed()) copy.fix();
    return copy;
  }
}
