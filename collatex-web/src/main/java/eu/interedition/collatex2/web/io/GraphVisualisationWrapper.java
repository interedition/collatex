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

package eu.interedition.collatex2.web.io;

import java.util.Iterator;

import org.jgrapht.alg.DijkstraShortestPath;

import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.web.ApiWitness;

public class GraphVisualisationWrapper {
  private final IVariantGraph graph;
  private final ApiWitness[] witnesses;
  private final IVariantGraphVertex startvertex;

  public GraphVisualisationWrapper(ApiWitness[] witnesses, IVariantGraph graph) {
    this.witnesses = witnesses;
    this.graph = graph;
    startvertex = graph.getStartVertex();
  }

  public String getJson() {
    StringBuilder jsonBuilder = new StringBuilder("[");
    Iterator<IVariantGraphVertex> iterator = graph.iterator();
    while (iterator.hasNext()) {
      IVariantGraphVertex vertex = iterator.next();
      String vertexId = vertex.toString();
      jsonBuilder.append("{").//
          append("'id':'").append(vertexId).append("',").//
          append("'name':'").append(vertex.getNormalized()).append("',").//
          append("'data':{").//
          append("'$color':'").append(color(vertex)).append("',").//
          append("'$type':'").append(type(vertex)).append("',").//
          append("'$dim':10},").//
          append("'adjacencies':[");
      for (IVariantGraphEdge edge : graph.outgoingEdgesOf(vertex)) {
        jsonBuilder.append("{'nodeFrom':'").append(vertexId).//
            append("','nodeTo':'").append(graph.getEdgeTarget(edge).toString()).//
            append("','data':{'$color':'grey','$type':'arrow'}},");
      }
      jsonBuilder.append("]},");
    }

    jsonBuilder.append("]");
    return jsonBuilder.toString();
  }

  private String type(IVariantGraphVertex vertex) {
    if (graph.inDegreeOf(vertex) == 0 || graph.outDegreeOf(vertex) == 0) {
      return "star";
    } else if (!(graph.inDegreeOf(vertex) == 1 && graph.outDegreeOf(vertex) == 1)) {
      return "triangle";
    }
    return "circle";
  }

  private static final String[] COLORS = { "grey", "blue", "green", "red", "brown", "orange", "purple", "cyan", "taupe", "yellow", "lightred" };

  //  private String color(IVariantGraphVertex vertex) {
  //    if (vertex.getWitnesses().size() == 0) {
  //      return COLORS[0];
  //    } else {
  //      int i = 1;
  //      while (i < COLORS.length) {
  //        if (vertex.containsWitness(witnesses[i].getSigil())) {
  //          return COLORS[i + 1];
  //        }
  //        i += 1;
  //      }
  //    }
  //    return "white";
  //  }

  private String color(IVariantGraphVertex vertex) {
    if (vertex.getWitnesses().size() == 0) {
      return COLORS[0];
    } 
    DijkstraShortestPath<IVariantGraphVertex, IVariantGraphEdge> dsp = new DijkstraShortestPath<IVariantGraphVertex, IVariantGraphEdge>(graph, startvertex, vertex);
    int topologicalIndex = (int) dsp.getPathLength();
    if (topologicalIndex > COLORS.length) {
      return "white";
    }
    return COLORS[topologicalIndex];
  }

  public ApiWitness[] getWitnesses() {
    return witnesses;
  }
}
