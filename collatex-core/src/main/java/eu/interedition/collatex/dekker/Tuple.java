/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.dekker;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Tuple<T> {

  public final T left;
  public final T right;

  private final Set<Object> set;

  public Tuple(T left, T right) {
    this.left = left;
    this.right = right;
    this.set = Sets.<Object>newHashSet(left, right);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Tuple) {
      return set.equals(((Tuple) obj).set);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return set.hashCode();
  }
}
