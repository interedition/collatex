/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.simple;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

import java.util.Collections;

public class SimpleToken implements Token, Comparable<SimpleToken> {
  public static final SimpleToken START = new SimpleToken(SimpleWitness.SUPERBASE, -1, "", "#");
  public static final SimpleToken END = new SimpleToken(SimpleWitness.SUPERBASE, Integer.MAX_VALUE, "", "#");

  final Witness witness;
  final int index;
  final String content;
  final String normalized;

  public SimpleToken(Witness witness, int index, String content, String normalized) {
    this.witness = witness;
    this.index = index;
    this.content = content;
    this.normalized = normalized;
  }

  public int getIndex() {
    return index;
  }

  public String getContent() {
    return content;
  }

  @Override
  public Witness getWitness() {
    return witness;
  }

  public String getNormalized() {
    return normalized;
  }

  @Override
  public String toString() {
    return new StringBuilder(witness.toString()).append(":").append(index).append(":'").append(normalized).append("'").toString();
  }

  public static String toString(Iterable<? extends Token> tokens) {
    final StringBuilder normalized = new StringBuilder();
    for (SimpleToken token : Iterables.filter(tokens, SimpleToken.class)) {
      normalized.append(token.getNormalized()).append(" ");
    }
    return normalized.toString().trim();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getIndex(), getWitness());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof SimpleToken) {
      final SimpleToken other = (SimpleToken) obj;
      return index == other.index && witness.equals(other.witness);
    }
    return super.equals(obj);
  }

  @Override
  public int compareTo(SimpleToken o) {
    Preconditions.checkArgument(witness.equals(o.witness));
    return (index - o.index);
  }

    public static final Function<VariantGraph.Vertex, String> VERTEX_TO_STRING = new Function<VariantGraph.Vertex, String>() {
        @Override
        public String apply(VariantGraph.Vertex input) {
            final Witness witness = Iterables.getFirst(input.witnesses(), null);
            if (witness == null) {
                return "";
            }
            final StringBuilder contents = new StringBuilder();
            for (SimpleToken token : Ordering.natural().sortedCopy(Iterables.filter(input.tokens(Collections.singleton(witness)), SimpleToken.class))) {
                contents.append(token.getContent()).append(" ");
            }
            return contents.toString().trim();
        }
    };
}
