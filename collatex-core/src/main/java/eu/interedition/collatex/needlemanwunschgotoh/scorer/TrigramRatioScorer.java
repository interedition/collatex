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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Calculate a score based on trigram ratio.
 *
 * Trigram ratio is defined as:
 *   ta  = number of trigrams in a
 *   tb  = number of trigrams in b
 *   tab = number of trigrams in that are both in a and in b
 *   ratio = 2 * tab / (ta + tb)
 *
 * @author Marcello Perathoner
 */
public class TrigramRatioScorer extends AbstractStringMetricScorer {

    final Map<String, Set<String>> trigrams = new HashMap<>();

    public TrigramRatioScorer() {
        super();
    }

    public TrigramRatioScorer(final double minScore, final double maxScore) {
        super(minScore, maxScore);
    }

    private Set<String> trigramize(final String s) {
        assert s.length() > 0;

        Set<String> tri = new HashSet<>();
        String ss = "  " + s + "  ";

        for (int i = 0; i < ss.length() - 2; i++) {
            tri.add(ss.substring(i, i + 3));
        }
        return tri;
    }

    @Override
    protected double _score(final Pair<String, String> p) {
        if (p.a.equals(p.b)) {
            return maxScore;
        }

        Set<String> triA = trigrams.computeIfAbsent(p.a, this::trigramize);
        Set<String> triB = trigrams.computeIfAbsent(p.b, this::trigramize);

        Set<String> triAB = new HashSet<>(triA);
        triAB.retainAll(triB);

        final double ratio = 2.0 * triAB.size() / (triA.size() + triB.size());

        return minScore + (maxScore - minScore) * ratio;
    }
}
