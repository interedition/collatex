package de.tud.kom.stringmatching.gst;

/**
 *
 * This file is part of Shingle Cloud Library, Copyright (C) 2009 Arno Mittelbach, Lasse Lehmann
 *
 * Shingle Cloud Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shingle Cloud Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Shingle Cloud Library.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */


import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Greedy String Tiling.
 * <p/>
 * <p>
 * Implements the Greedy String Tiling algorithm as proposed by Michael J. Wise in his paper:
 * "String Similarity via Greedy String Tiling and Running Karp-Rabin Matching"
 * </p>
 *
 * @author Arno Mittelbach
 */
public class GreedyStringTilingAlgorithm extends CollationAlgorithm.Base {

  @Override
  public void collate(VariantGraph graph, Iterable<Token> witness) {
    //final Token[] tokens = Iterables.toArray(witness, Token.class);
    throw new UnsupportedOperationException();
  }

  public static <T> SortedSet<Match> match(T[] left, T[] right, Comparator<T> comparator, int minimumTileLength) {
    final boolean[] markedLeft = new boolean[left.length];
    final boolean[] markedRight = new boolean[right.length];

    Arrays.fill(markedLeft, false);
    Arrays.fill(markedRight, false);

    final SortedSet<Match> matches = new TreeSet<Match>();
    final Map<Integer, List<Match>> matchesByLength = new HashMap<Integer, List<Match>>();

    int maxMatchLength;
    do {
      maxMatchLength = minimumTileLength;
      for (int rc = 0; rc < right.length; rc++) {
        for (int lc = 0; lc < left.length; lc++) {
          int matchLength = 0;
          for (int tc = 0;
               (tc + lc) < left.length && (tc + rc) < right.length &&
               !markedLeft[lc + tc] && !markedRight[rc + tc] &&
               comparator.compare(left[lc + tc], right[rc + tc]) == 0;
               tc++) {
            matchLength++;
          }

          if (matchLength >= maxMatchLength) {
            List<Match> theMatches = matchesByLength.get(matchLength);
            if (theMatches == null) {
              matchesByLength.put(matchLength, theMatches = new ArrayList<Match>());
            }
            theMatches.add(new Match(lc, rc));
          }

          if (matchLength > maxMatchLength) {
            maxMatchLength = matchLength;
          }
        }
      }

      for (Match match : Objects.firstNonNull(matchesByLength.get(maxMatchLength), Collections.<Match>emptyList())) {
        boolean occluded = false;

        for (int tc = 0; tc < maxMatchLength; tc++) {
          if (markedLeft[match.left + tc] || markedRight[match.right + tc]) {
            occluded = true;
            break;
          }
        }

        if (!occluded) {
          for (int tc = 0; tc < maxMatchLength; tc++) {
            markedLeft[match.left + tc] = true;
            markedRight[match.right + tc] = true;
          }
          matches.add(new Match(match.left, match.right, maxMatchLength));
        }
      }

    } while (maxMatchLength > minimumTileLength);

    return matches;
  }

  public static class Match implements Comparable<Match> {
    public final int left;
    public final int right;
    public final int length;

    public Match(int left, int right, int length) {
      this.left = left;
      this.right = right;
      this.length = length;
    }

    public Match(int left, int right) {
      this(left, right, 0);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof Match) {
        return (left == ((Match) obj).left);
      }
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      return left;
    }

    @Override
    public int compareTo(Match o) {
      return left - o.left;
    }
  }
}
