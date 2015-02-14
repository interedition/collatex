/*
 * Copyright (c) 2015 The Interedition Development Group.
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

import java.util.Objects;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Tuple<T> {

    public final T left;
    public final T right;

    public Tuple(T left, T right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Tuple) {
            final Tuple other = (Tuple) obj;
            return (left.equals(other.left) || left.equals(other.right)) && (right.equals(other.right) || right.equals(other.left));
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
