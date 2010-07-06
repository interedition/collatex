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
import eu.interedition.collatex.input.BaseElement;

public class Tuple2<T> {
  final T left;
  final T right;
  final Match<BaseElement> _nextMatch;

  public Tuple2(final T _left, final T _right, final Match nextMatch) {
    this.left = _left;
    this.right = _right;
    this._nextMatch = nextMatch;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Tuple2)) return false;
    final Tuple2<?> other_tuple = (Tuple2<?>) obj;
    return other_tuple.left.equals(left) && other_tuple.right.equals(right);
  }

  @Override
  public int hashCode() {
    return left.hashCode() + right.hashCode();
  }

  @Override
  public String toString() {
    return "{" + left.toString() + "; " + right.toString() + "}";
  }
}
