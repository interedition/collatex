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

package eu.interedition.collatex.alignment;

import eu.interedition.collatex.input.BaseElement;

public class Match<T extends BaseElement> implements Comparable<Match> {
  private final T word1;
  private final T word2;
  public final float wordDistance;

  public Match(final T baseWord, final T witnessWord) {
    this(baseWord, witnessWord, 0);
  }

  public Match(final T baseWord, final T witnessWord, final float levDistance) {
    this.word1 = baseWord;
    this.word2 = witnessWord;
    this.wordDistance = levDistance;
  }

  @Override
  public String toString() {
    return "(" + word1.getBeginPosition() + "->" + word2.getBeginPosition() + ")";
  }

  public T getWitnessWord() {
    return word2;
  }

  public T getBaseWord() {
    return word1;
  }

  @Override
  public boolean equals(final Object _other) {
    if (!(_other instanceof Match)) {
      return false;
    }
    final Match other = (Match) _other;
    return this.word1.equals(other.word1) && this.word2.equals(other.word2);
  }

  @Override
  public int hashCode() {
    return word1.hashCode() + word2.hashCode();
  }

  @Override
  public int compareTo(final Match m2) {
    return getBaseWord().getBeginPosition() - m2.getBaseWord().getBeginPosition();
  }
}
