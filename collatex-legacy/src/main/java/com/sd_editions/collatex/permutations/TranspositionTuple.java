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

import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.BaseElement;

public class TranspositionTuple<T extends BaseElement> {

  private final Tuple2<MatchSequence<T>> tuple;

  public TranspositionTuple(final Tuple2<MatchSequence<T>> _tuple) {
    this.tuple = _tuple;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof TranspositionTuple)) {
      return false;
    }
    final TranspositionTuple tuple2 = (TranspositionTuple) obj;
    final boolean result = tuple2.getLeftWordCode().equals(getLeftWordCode()) && tuple2.getRightWordCode().equals(getRightWordCode());
    //    System.out.println("comparing: " + this.toString() + " && " + tuple2.toString() + " result: " + result);
    return result;
  }

  @Override
  public int hashCode() {
    return getLeftWordCode().hashCode() + getRightWordCode().hashCode();
  }

  @Override
  public String toString() {
    return tuple.toString();
  }

  MatchSequence getLeftSequence() {
    return tuple.left;
  }

  MatchSequence getRightSequence() {
    return tuple.right;
  }

  Integer getRightWordCode() {
    return getRightSequence().code;
  }

  Integer getLeftWordCode() {
    return getLeftSequence().code;
  }

}
