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

package eu.interedition.collatex.matching;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

public class Matches {

  private final ListMultimap<Token, VariantGraph.Vertex> all;
  private final Set<Token> unmatched;
  private final Set<Token> ambiguous;
  private final Set<Token> unique;

  public static Matches between(final Iterable<VariantGraph.Vertex> vertices, final Iterable<Token> witnessTokens, Comparator<Token> comparator) {

    final ListMultimap<Token, VariantGraph.Vertex> all = ArrayListMultimap.create();
    for (VariantGraph.Vertex vertex : vertices) {
      final Set<Token> tokens = vertex.tokens();
      if (tokens.isEmpty()) {
        continue;
      }
      for (Token witnessToken : witnessTokens) {
        if (comparator.compare(Iterables.getFirst(tokens, null), witnessToken) == 0) {
          all.put(witnessToken, vertex);
        }
      }
    }

    // unmatched tokens
    Set<Token> unmatched = Sets.newLinkedHashSet();
    for (Token witnessToken : witnessTokens) {
      if (!all.containsKey(witnessToken)) {
        unmatched.add(witnessToken);
      }
    }
    // unsure tokens (have to check: base -> witness, and witness -> base)
    Set<Token> ambiguous = Sets.newLinkedHashSet();
    for (Token witnessToken : witnessTokens) {
      int count = all.keys().count(witnessToken);
      if (count > 1) {
        ambiguous.add(witnessToken);
      }
    }
    Multiset<VariantGraph.Vertex> bag = ImmutableMultiset.copyOf(all.values());
    Set<VariantGraph.Vertex> unsureBaseTokens = Sets.newLinkedHashSet();
    for (VariantGraph.Vertex baseToken : vertices) {
      int count = bag.count(baseToken);
      if (count > 1) {
        unsureBaseTokens.add(baseToken);
      }
    }
    Collection<Map.Entry<Token, VariantGraph.Vertex>> entries = all.entries();
    for (Map.Entry<Token, VariantGraph.Vertex> entry : entries) {
      if (unsureBaseTokens.contains(entry.getValue())) {
        ambiguous.add(entry.getKey());
      }
    }
    // sure tokens
    // have to check unsure tokens because of (base -> witness && witness -> base)
    Set<Token> unique = Sets.newLinkedHashSet();
    for (Token witnessToken : witnessTokens) {
      if (all.keys().count(witnessToken) == 1 && !ambiguous.contains(witnessToken)) {
        unique.add(witnessToken);
      }
    }

    return new Matches(all, unmatched, ambiguous, unique);
  }

  private Matches(ListMultimap<Token, VariantGraph.Vertex> all, Set<Token> unmatched, Set<Token> ambiguous, Set<Token> unique) {
    this.all = all;
    this.unmatched = unmatched;
    this.ambiguous = ambiguous;
    this.unique = unique;
  }

  public ListMultimap<Token, VariantGraph.Vertex> getAll() {
    return all;
  }

  public Set<Token> getUnmatched() {
    return unmatched;
  }

  public Set<Token> getAmbiguous() {
    return ambiguous;
  }

  public Set<Token> getUnique() {
    return unique;
  }

}
