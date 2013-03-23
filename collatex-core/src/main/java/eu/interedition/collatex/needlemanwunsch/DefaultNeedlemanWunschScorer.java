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

package eu.interedition.collatex.needlemanwunsch;

import com.google.common.collect.Iterables;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

import java.util.Comparator;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DefaultNeedlemanWunschScorer implements NeedlemanWunschScorer<Set<VariantGraph.Vertex>, Token> {

  private final Comparator<Token> comparator;

  public DefaultNeedlemanWunschScorer(Comparator<Token> comparator) {
    this.comparator = comparator;
  }

  @Override
  public float score(Set<VariantGraph.Vertex> a, Token b) {
    for (VariantGraph.Vertex vertex : a) {
      final Set<Token> tokens = vertex.tokens();
      if (!tokens.isEmpty() && comparator.compare(Iterables.getFirst(tokens, null), b) == 0) {
        return 1;
      }
    }
    return -1;
  }

  @Override
  public float gap() {
    return -1;
  }
}
