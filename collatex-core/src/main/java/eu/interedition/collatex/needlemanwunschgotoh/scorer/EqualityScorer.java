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
 * Calculate a score based on string equality.
 *
 * Score is max if strings match, min otherwise.
 *
 * @author Marcello Perathoner
 */
public class EqualityScorer extends AbstractStringMetricScorer {

    public EqualityScorer() {
        super();
    }

    public EqualityScorer(final double minScore, final double maxScore) {
        super(minScore, maxScore);
    }

    @Override
    public double score(final String a, final String b) {
        return a.equals(b) ? maxScore : minScore;
    }
}
