/*
 * Copyright (c) 2013 The Interedition Development Group.
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

import com.google.common.base.Function;
import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.dekker.matrix.MatchTableLinker;
import eu.interedition.collatex.util.VertexMatch;
import eu.interedition.collatex.medite.MediteAlgorithm;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm;

import java.util.Comparator;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author Ronald Haentjens Dekker
 */
public class CollationAlgorithmFactory {

  public static CollationAlgorithm dekker(Comparator<Token> comparator) {
    return dekkerMatchMatrix(comparator, 3);
  }

  public static CollationAlgorithm dekkerMatchMatrix(Comparator<Token> comparator, int outlierTranspositionsSizeLimit) {
    return new DekkerAlgorithm(comparator, new MatchTableLinker(outlierTranspositionsSizeLimit));
  }

  public static CollationAlgorithm needlemanWunsch(Comparator<Token> comparator) {
    return new NeedlemanWunschAlgorithm(comparator);
  }

  public static CollationAlgorithm medite(Comparator<Token> comparator, Function<SortedSet<VertexMatch.WithToken>, Integer> matchEvaluator) {
    return new MediteAlgorithm(comparator, matchEvaluator);
  }
}
