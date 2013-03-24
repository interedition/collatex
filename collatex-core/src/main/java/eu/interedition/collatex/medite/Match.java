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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

import javax.annotation.Nullable;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public abstract class Match implements Comparable<Match> {
  public final VariantGraph.Vertex vertex;
  public final int vertexRank;

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
  public static class WithToken extends Match {

    public final Token token;

    WithToken(VariantGraph.Vertex vertex, int vertexRank, Token token) {
      super(vertex, vertexRank);
      this.token = token;
    }

    @Override
    public String toString() {
      return "{" + vertex + " -> " + token + "}";
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

  static Function<WithTokenIndex, WithToken> tokenResolver(final Token[] tokens) {
    return new Function<WithTokenIndex, WithToken>() {
      @Override
      public WithToken apply(@Nullable WithTokenIndex input) {
        return new WithToken(input.vertex, input.vertexRank, tokens[input.token]);
      }
    };
  }

  static final Predicate<Phrase<WithTokenIndex>> filter(final IndexRangeSet rankFilter, final IndexRangeSet tokenFilter) {
    return new Predicate<Phrase<WithTokenIndex>>() {
      @Override
      public boolean apply(@Nullable Phrase<WithTokenIndex> input) {
        for (WithTokenIndex match : input) {
          if (tokenFilter.apply(match.token) || rankFilter.apply(match.vertexRank)) {
            return true;
          }
        }
        return false;
      }
    };
  }
}
