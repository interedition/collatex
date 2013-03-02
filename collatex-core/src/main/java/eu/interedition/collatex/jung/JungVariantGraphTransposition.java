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
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import eu.interedition.collatex.VariantGraph;

import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JungVariantGraphTransposition implements VariantGraph.Transposition {

  private final JungVariantGraph graph;
  private final Set<VariantGraph.Vertex> vertices;

  public JungVariantGraphTransposition(JungVariantGraph graph, Set<VariantGraph.Vertex> vertices) {
    this.graph = graph;
    this.vertices = Sets.newHashSet(vertices);
    for (VariantGraph.Vertex vertex : this.vertices) {
      graph.transpositionIndex.put(vertex, this);
    }
  }

  @Override
  public void delete() {
    for (VariantGraph.Vertex vertex : this.vertices) {
      graph.transpositionIndex.remove(vertex, this);
    }
  }

  @Override
  public Iterator<VariantGraph.Vertex> iterator() {
    return vertices.iterator();
  }

  @Override
  public String toString() {
    return Iterables.toString(vertices);
  }
}
