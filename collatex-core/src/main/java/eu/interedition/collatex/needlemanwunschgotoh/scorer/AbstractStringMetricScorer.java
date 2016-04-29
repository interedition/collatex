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

import java.util.HashMap;
import java.util.Map;

/**
 * Calculate a score based on string metrics. Skeletal implementation.
 *
 * Note that the minimum score is the score for a mismatch.  This score must be
 * higher than two times the score of a gap.  If a mismatch would score less
 * than two gaps, the aligner would always select two gaps and never select a
 * mismatch.
 *
 * This implementation caches the calculated score.
 *
 * @author Marcello Perathoner
 */
public abstract class AbstractStringMetricScorer implements StringMetricScorer {

    /** Memoization cache. Caches the score of already computed pairs. */
    private final Map<Pair<String, String>, Double> cache = new HashMap<>();

    /** The minimum score. */
    protected final double minScore;

    /** The maxiumum score. */
    protected final double maxScore;

    /** Constructor. */
    public AbstractStringMetricScorer() {
        this(-1.0, 1.0);
    }

    /**
     * Constructor.
     *
     * @param minScore The score for the worst match.
     * @param maxScore The score for the best match.
     */
    public AbstractStringMetricScorer(final double minScore, final double maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    /**
     * Return the minimun score this scorer will ever calculate.
     *
     * @return The score for the worst match.
     */
    public double getMinScore() {
        return minScore;
    };

    /**
     * Return the maximun score this scorer will ever calculate.
     *
     * @return The score for the best match.
     */
    public double getMaxScore() {
        return maxScore;
    };

    /** Calculate the score. Override this for cached scorers. */
    protected double _score(final Pair<String, String> p) {
        return 0.0;
    }

    /** Calculate the score. Override this for uncached scorers. */
    public double score(final String a, final String b) {
        final Pair<String, String> pair = new Pair<>(a, b);

        return cache.computeIfAbsent(pair, p -> _score(p));
    }
}
