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

package eu.interedition.collatex.matching;

/**
 * Calculate a score based on string metrics.
 *
 * @author Marcello Perathoner
 */
public interface StringMetricScorer {

    /** Calculate the score for a match between and b. */
    double score(String a, String b);

    /** Return the minimun score this scorer will ever calculate. */
    double getMinScore();

    /** Return the maximun score this scorer will ever calculate. */
    double getMaxScore();
}
