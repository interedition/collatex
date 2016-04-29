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
 * Calculate a score based on the Levenshtein distance.
 *
 * Score is 1 if distance &lt;= threshold, -1 otherwise.
 *
 * @author Marcello Perathoner
 */
public class LevenshteinDistanceScorer extends AbstractStringMetricScorer {

    final private int threshold;

    public LevenshteinDistanceScorer(final int threshold) {
        super();
        this.threshold = threshold;
    }

    public LevenshteinDistanceScorer(final double minScore,
                                     final double maxScore,
                                     final int threshold) {
        super(minScore, maxScore);
        this.threshold = threshold;
    }

    @Override
    protected double _score(final Pair<String, String> p) {
        if (p.a.equals(p.b)) {
            return 1.0;
        }
        return EditDistance.compute(p.a, p.b) <= threshold ? maxScore : minScore;
    }
}
