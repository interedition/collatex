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

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.Comparator;

public class EditDistanceRatioTokenComparator implements Comparator<Token> {

    private final double threshold;
    private final LevenshteinRatioScorer scorer;

    public EditDistanceRatioTokenComparator() {
        this(0.6);
    }

    public EditDistanceRatioTokenComparator(double threshold) {
        this.threshold = threshold;
        this.scorer = new LevenshteinRatioScorer();
    }

    @Override
    public int compare(Token token_a, Token token_b) {
        final String a = ((SimpleToken) token_a).getNormalized();
        final String b = ((SimpleToken) token_b).getNormalized();
        return (scorer.score(a, b) >= threshold) ? 0 : a.compareTo(b);
    }
}
