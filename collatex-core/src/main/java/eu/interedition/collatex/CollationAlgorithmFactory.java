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

package eu.interedition.collatex;

import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.dekker.editgraphaligner.EditGraphAligner;
import eu.interedition.collatex.medite.MediteAlgorithm;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm;
import eu.interedition.collatex.util.GreedyStringTilingAlgorithm;
import eu.interedition.collatex.util.VertexMatch;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.function.Function;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 * @author Ronald Haentjens Dekker
 */
public class CollationAlgorithmFactory {

    public static CollationAlgorithm dekker(Comparator<Token> comparator) {
        return new EditGraphAligner(comparator);
    }

    public static CollationAlgorithm legacyDekker(Comparator<Token> comparator) {
        return new DekkerAlgorithm(comparator);
    }

    public static CollationAlgorithm needlemanWunsch(Comparator<Token> comparator) {
        return new NeedlemanWunschAlgorithm(comparator);
    }

    public static CollationAlgorithm greedyStringTiling(Comparator<Token> comparator, int minimumTileLength) {
        return new GreedyStringTilingAlgorithm(comparator, minimumTileLength);
    }

    public static CollationAlgorithm medite(Comparator<Token> comparator, Function<SortedSet<VertexMatch.WithToken>, Integer> matchEvaluator) {
        return new MediteAlgorithm(comparator, matchEvaluator);
    }
}
