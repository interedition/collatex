/*
 * Copyright (c) 2016 The Interedition Development Group.
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

package eu.interedition.collatex.needlemanwunschgotoh;

/**
 * A scorer for a {@code NeedlemanWunschGotohAligner}.
 *
 * Calculates the score of a match between two generic objects.
 *
 * @param <A> Type of the first object
 * @param <B> Type of the second object
 *
 * @author Marcello Perathoner
 */
public interface NeedlemanWunschScorer<A, B> {
    /**
     * Calculate the score given to a match between a and b.
     *
     * @param a An object
     * @param b An object
     *
     * @return The score
     */
    double score(A a, B b);
}
