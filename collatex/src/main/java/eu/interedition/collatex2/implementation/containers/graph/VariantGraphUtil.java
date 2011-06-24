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

package eu.interedition.collatex2.implementation.containers.graph;

import java.util.List;

import org.jgrapht.alg.BellmanFordShortestPath;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

public class VariantGraphUtil {
  private final VariantGraph graph;

  public VariantGraphUtil(VariantGraph graph) {
    this.graph = graph;
  }

  //TODO: should getLongestPath() method return IVariantGraphEdges?
  public List<IVariantGraphVertex> getLongestPath() {
    // NOTE: Weights are set to negative value to
    // generate the longest path instead of the shortest path
    for (IVariantGraphEdge edge : graph.edgeSet()) {
      graph.setEdgeWeight(edge, -1);
    }
    // NOTE: gets the start vertex of the graph
    IVariantGraphVertex startVertex = graph.getStartVertex();
    IVariantGraphVertex endVertex = graph.getEndVertex();
    // Note: calculates the longest path
    List<IVariantGraphEdge> findPathBetween = BellmanFordShortestPath.findPathBetween(graph, startVertex, endVertex);
    // Note: gets the end vertices associated with the edges of the path
    List<IVariantGraphVertex> vertices = Lists.newArrayList();
    for (IVariantGraphEdge edge : findPathBetween) {
      IVariantGraphVertex edgeTarget = graph.getEdgeTarget(edge);
      if (edgeTarget != endVertex) {
        vertices.add(edgeTarget);
      }
    }
    return vertices;
  }

}
