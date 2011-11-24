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

package eu.interedition.collatex2.implementation.graph.joined;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

public class JVariantGraph extends DirectedAcyclicGraph<IJVariantGraphVertex, IJVariantGraphEdge> implements IJVariantGraph {
  private static final long serialVersionUID = 1L;
  private IJVariantGraphVertex startVertex;
  private IJVariantGraphVertex endVertex;

  public JVariantGraph() {
    super(IJVariantGraphEdge.class);
    //    startVertex = new JVariantGraphVertex("#");
    //    addVertex(startVertex);
    //    endVertex = new JVariantGraphVertex("#");
    //    addVertex(getEndVertex());
  }

  public static IJVariantGraph create() {
    return new JVariantGraph();
  }

  @Override
  public IJVariantGraphVertex getStartVertex() {
    return startVertex;
  }

  @Override
  public IJVariantGraphVertex getEndVertex() {
    return endVertex;
  }

  @Override
  public void setStartVertex(IJVariantGraphVertex startVertex) {
    if (!containsVertex(startVertex)) {
      addVertex(startVertex);
    }
    this.startVertex = startVertex;
  }

  @Override
  public void setEndVertex(IJVariantGraphVertex endVertex) {
    if (!containsVertex(endVertex)) {
      addVertex(endVertex);
    }
    this.endVertex = endVertex;
  }

}
