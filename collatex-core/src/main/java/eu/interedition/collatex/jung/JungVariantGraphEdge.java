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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JungVariantGraphEdge implements VariantGraph.Edge {

  final JungVariantGraph graph;
  final Set<Witness> witnesses;

  public JungVariantGraphEdge(JungVariantGraph graph, Set<Witness> witnesses) {
    this.graph = graph;
    this.witnesses = Sets.newHashSet(witnesses);
  }

  @Override
  public VariantGraph.Edge add(Set<Witness> witnesses) {
    this.witnesses.addAll(witnesses);
    return this;
  }

  @Override
  public Set<Witness> witnesses() {
    return Collections.unmodifiableSet(witnesses);
  }

  @Override
  public VariantGraph graph() {
    return graph;
  }

  @Override
  public VariantGraph.Vertex from() {
    return graph.getEndpoints(this).getFirst();
  }

  @Override
  public VariantGraph.Vertex to() {
    return graph.getEndpoints(this).getSecond();
  }

  @Override
  public void delete() {
    graph.removeEdge(this);
  }

  @Override
  public String toString() {
    return Iterables.toString(witnesses);
  }
}
