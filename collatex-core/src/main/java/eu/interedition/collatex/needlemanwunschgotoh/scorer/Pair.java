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

package eu.interedition.collatex.matching;

/**
 * A Pair class that can be used as key in Map.
 *
 * @author Marcello Perathoner
 */
public class Pair<T, U> {
    public final T a;
    public final U b;

    public Pair(T a, U b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Pair) {
            Pair other = (Pair) obj;
            return a.equals(other.a) && b.equals(other.b);
        }
        return false;
    }

    @Override
    public int hashCode() {
        // Map.Entry uses operator ^ but + is a better choice because
        // we also want to store Pairs of identic strings.
        return a.hashCode() + b.hashCode();
    }

    public String toString() {
        return "(" + a.toString() + ", " + b.toString() + ")";
    }
}
