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

package eu.interedition.collatex.experimental.ngrams.alignment;

import eu.interedition.collatex.experimental.ngrams.NGram;

public class Gap {
  private final NGram gapA;
  private final NGram gapB;
  private final NGram nextMatchA;

  public Gap(final NGram gapA, final NGram gapB, final NGram nextMatchA) {
    this.gapA = gapA;
    this.gapB = gapB;
    this.nextMatchA = nextMatchA;
  }

  @Override
  public String toString() {
    if (isAddition()) {
      return "\"" + gapB.getNormalized() + "\" added";
    }
    return "A: " + gapA.getNormalized() + " -> B: " + gapB.getNormalized();
  }

  public NGram getNGramA() {
    return gapA;
  }

  public NGram getNGramB() {
    return gapB;
  }

  public boolean isEmpty() {
    return gapA.isEmpty() && gapB.isEmpty();
  }

  public boolean isReplacement() {
    return !gapA.isEmpty() && !gapB.isEmpty();
  }

  public boolean isAddition() {
    return gapA.isEmpty() && !gapB.isEmpty();
  }

  private boolean isOmission() {
    return !gapA.isEmpty() && gapB.isEmpty();
  }

  public Modification getModification() {
    if (isAddition()) {
      return createAddition();
    }
    if (isOmission()) {
      return createOmission();
    }
    if (isReplacement()) {
      return createReplacement();
    }
    throw new RuntimeException("Not a modification!");
  }

  private Modification createReplacement() {
    return new Replacement(gapA, gapB);
  }

  private Modification createOmission() {
    return new Omission(gapA);
  }

  // TODO 0 -> this is not right!
  private Modification createAddition() {
    return new Addition(nextMatchA, gapB);
  }

}
