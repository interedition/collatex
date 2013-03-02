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

package eu.interedition.collatex.jung;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JungVariantGraphVertex implements VariantGraph.Vertex {
  private final JungVariantGraph graph;
  private final Set<Token> tokens;

  public JungVariantGraphVertex(JungVariantGraph graph, Set<Token> tokens) {
    this.graph = graph;
    this.tokens = Sets.newHashSet(tokens);
  }

  @Override
  public Iterable<? extends VariantGraph.Edge> incoming() {
    return incoming(null);
  }

  @Override
  public Iterable<? extends VariantGraph.Edge> incoming(final Set<Witness> witnesses) {
    return paths(graph.getInEdges(this), witnesses);
  }

  @Override
  public Iterable<? extends VariantGraph.Edge> outgoing() {
    return outgoing(null);
  }

  @Override
  public Iterable<? extends VariantGraph.Edge> outgoing(Set<Witness> witnesses) {
    return paths(graph.getOutEdges(this), witnesses);
  }

  @Override
  public Iterable<? extends VariantGraph.Transposition> transpositions() {
    return graph.transpositionIndex.get(this);
  }

  @Override
  public Set<Token> tokens() {
    return tokens(null);
  }

  @Override
  public Set<Token> tokens(final Set<Witness> witnesses) {
    return Collections.unmodifiableSet(Sets.filter(tokens, witnesses == null ? Predicates.<Token>alwaysTrue() : new Predicate<Token>() {
      @Override
      public boolean apply(@Nullable Token token) {
        return witnesses.contains(token.getWitness());
      }
    }));
  }

  @Override
  public Set<Witness> witnesses() {
    final Set<Witness> witnesses = Sets.newHashSet();
    for (VariantGraph.Edge edge : incoming()) {
      witnesses.addAll(edge.witnesses());
    }
    return witnesses;
  }

  @Override
  public void add(Iterable<Token> tokens) {
    Iterables.addAll(this.tokens, tokens);
  }

  @Override
  public VariantGraph graph() {
    return graph;
  }

  @Override
  public void delete() {
    graph.removeVertex(this);
  }

  @Override
  public String toString() {
    return Iterables.toString(tokens);
  }

  protected static Iterable<? extends VariantGraph.Edge> paths(final Iterable<JungVariantGraphEdge> edges, final Set<Witness> witnesses) {
    return Iterables.filter(edges, (witnesses == null ? Predicates.<JungVariantGraphEdge>alwaysTrue() : new Predicate<JungVariantGraphEdge>() {
      @Override
      public boolean apply(@Nullable JungVariantGraphEdge edge) {
        for (Witness edgeWitness : edge.witnesses()) {
          if (witnesses.contains(edgeWitness)) {
            return true;
          }
        }
        return false;
      }
    }));
  }
}
