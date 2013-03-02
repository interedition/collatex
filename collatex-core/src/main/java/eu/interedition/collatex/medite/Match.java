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

package eu.interedition.collatex.medite;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
abstract class Match implements Comparable<Match> {
  final VariantGraph.Vertex vertex;
  final int vertexRank;

  Match(VariantGraph.Vertex vertex, int vertexRank) {
    this.vertex = vertex;
    this.vertexRank = vertexRank;
  }

  @Override
  public int compareTo(Match o) {
    return (vertexRank - o.vertexRank);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Match) {
      return vertexRank == ((Match)obj).vertexRank;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return vertexRank;
  }

  /**
  * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
  */
  static class WithEquivalence extends Match {
    final SuffixTree<Token>.EquivalenceClass equivalenceClass;

    WithEquivalence(VariantGraph.Vertex vertex, int vertexRank, SuffixTree<Token>.EquivalenceClass equivalenceClass) {
      super(vertex, vertexRank);
      this.equivalenceClass = equivalenceClass;
    }

    @Override
    public String toString() {
      return "{" + vertex + " -> " + equivalenceClass + "}";
    }
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  static class WithTokenIndex extends Match {

    final int token;

    WithTokenIndex(VariantGraph.Vertex vertex, int vertexRank, int token) {
      super(vertex, vertexRank);
      this.token = token;
    }

    @Override
    public String toString() {
      return "{" + vertex + " -> " + token + "}";
    }
  }
}
