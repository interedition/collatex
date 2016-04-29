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
 * Calculate a score based on Levenshtein Ratio.
 *
 * Levenshtein ratio as used in python-Levenshtein
 *   distance = levenshtein (a, b)
 *   length = length (a) + length (b)
 *   ratio = (length - distance) / length
 *
 * @author Marcello Perathoner
 */
public class LevenshteinRatioScorer extends AbstractStringMetricScorer {

    public LevenshteinRatioScorer() {
        super();
    }

    public LevenshteinRatioScorer(final double minScore, final double maxScore) {
        super(minScore, maxScore);
    }

    @Override
    protected double _score(final Pair<String, String> p) {
        if (p.a.equals(p.b)) {
            return maxScore;
        }
        final int distance = EditDistance.compute(p.a, p.b, 2);
        final int length   = p.a.length() + p.b.length();
        final double ratio = ((double) (length - distance)) / length;

        return minScore + (maxScore - minScore) * ratio;
    }
}
