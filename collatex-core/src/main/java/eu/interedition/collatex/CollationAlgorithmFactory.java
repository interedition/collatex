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
import eu.interedition.collatex.medite.MediteAlgorithm;
import eu.interedition.collatex.needlemanwunsch.*;
import eu.interedition.collatex.needlemanwunschgotoh.*;
import eu.interedition.collatex.matching.*;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.util.GreedyStringTilingAlgorithm;
import eu.interedition.collatex.util.VertexMatch;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 * @author Ronald Haentjens Dekker
 */
public class CollationAlgorithmFactory {
    protected final static Logger LOG = Logger.getLogger("CollationAlgorithmFactory");

    public static CollationAlgorithm dekker(Comparator<Token> comparator) {
        return new DekkerAlgorithm(comparator);
    }


    public static CollationAlgorithm needlemanWunsch(Comparator<Token> comparator) {
        return new eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm(comparator);
    }


    public static CollationAlgorithm needlemanWunschGotoh(StringMetricScorer scorer) {
        return new eu.interedition.collatex.needlemanwunschgotoh.NeedlemanWunschGotohAlgorithm(scorer);
    }

    public static CollationAlgorithm needlemanWunschGotoh() {
        return needlemanWunschGotoh(new TrigramRatioScorer());
    }


    public static CollationAlgorithm greedyStringTiling(Comparator<Token> comparator) {
        return greedyStringTiling(comparator, 2);
    }

    public static CollationAlgorithm greedyStringTiling(Comparator<Token> comparator,
                                                        Integer minimumTileLength) {
        return new GreedyStringTilingAlgorithm(comparator, minimumTileLength);
    }


    public static CollationAlgorithm medite(Comparator<Token> comparator) {
        return medite(comparator, SimpleToken.TOKEN_MATCH_EVALUATOR);
    }

    public static CollationAlgorithm medite(Comparator<Token> comparator,
            Function<SortedSet<VertexMatch.WithToken>, Integer> matchEvaluator) {
        return new MediteAlgorithm(comparator, matchEvaluator);
    }


    public static Comparator<Token> createComparator(String name, Object... args) {
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Comparator: {0}", name);
        }
        switch (name) {
        case "equality":
            new EqualityTokenComparator();
        case "levenshtein.distance":
            return args.length >= 1 ?
                new EditDistanceTokenComparator((Integer) args[0]) :
                new EditDistanceTokenComparator();
        case "levenshtein.ratio":
            return args.length >= 1 ?
                new EditDistanceRatioTokenComparator((Double) args[0]) :
                new EditDistanceRatioTokenComparator();
        }
        return new EqualityTokenComparator(); // default
    }

    public static CollationAlgorithm createAlgorithm(String name, Comparator<Token> comparator,
                                                     Object... args) {
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Algorithm: {0}", name);
        }
        switch (name) {
        case "dekker":
            return dekker(comparator);
        case "gst":
            return args.length >= 1 ?
                greedyStringTiling(comparator, (Integer) args[0]) :
                greedyStringTiling(comparator);
        case "medite":
            return args.length >= 1 ?
                medite(comparator, (Function<SortedSet<VertexMatch.WithToken>, Integer>) args[0]) :
                medite(comparator);
        case "needleman-wunsch":
            return needlemanWunsch(comparator);
        case "needleman-wunsch-gotoh":
            return args.length >= 1 ?
                needlemanWunschGotoh((eu.interedition.collatex.matching.StringMetricScorer) args[0]) :
                needlemanWunschGotoh();
        }
        return dekker(comparator); // default
    }
}
