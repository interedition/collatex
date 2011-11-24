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

package eu.interedition.collatex.implementation.graph.joined;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IVariantGraphEdge;
import eu.interedition.collatex.interfaces.IVariantGraphVertex;

public class JVariantGraphCreator {
  private static final Logger LOG = LoggerFactory.getLogger(JVariantGraphCreator.class);
  private IVariantGraph unjoinedGraph;
  private IJVariantGraph joinedGraph;
  private Map<IVariantGraphVertex, IJVariantGraphVertex> vertexMap;

  public IJVariantGraph parallelSegmentate(final IVariantGraph _unjoinedGraph) {
    unjoinedGraph = _unjoinedGraph;
    //    LOG.info("edges: {}", unjoinedGraph.edgeSet().size());
    joinedGraph = JVariantGraph.create();
    vertexMap = Maps.newHashMap();

    IVariantGraphVertex startVgVertex = unjoinedGraph.getStartVertex();
    JVariantGraphVertex startJvgVertex = new JVariantGraphVertex(startVgVertex);
    joinedGraph.setStartVertex(startJvgVertex);
    vertexMap.put(startVgVertex, startJvgVertex);

    IVariantGraphVertex endVgVertex = unjoinedGraph.getEndVertex();
    JVariantGraphVertex endJvgVertex = new JVariantGraphVertex(endVgVertex);
    joinedGraph.setEndVertex(endJvgVertex);
    vertexMap.put(endVgVertex, endJvgVertex);

    Set<IVariantGraphEdge> outgoingEdges = unjoinedGraph.outgoingEdgesOf(startVgVertex);
    for (IVariantGraphEdge vgEdge : outgoingEdges) {
      processEdge(vgEdge, startJvgVertex);
    }

    return joinedGraph;
  }

  private void processEdge(IVariantGraphEdge vgEdge, IJVariantGraphVertex lastJvgVertex) {
    //    LOG.info("edge: {} {}", vgEdge, vgEdge.hashCode());
    IVariantGraphVertex vgVertex = unjoinedGraph.getEdgeTarget(vgEdge);
    IJVariantGraphVertex jvgVertex;
    boolean vgVertexIsNew = true;
    if (vertexMap.containsKey(vgVertex)) {
      jvgVertex = vertexMap.get(vgVertex);
      vgVertexIsNew = false;
    } else {
      jvgVertex = new JVariantGraphVertex(vgVertex);
      vertexMap.put(vgVertex, jvgVertex);
      joinedGraph.addVertex(jvgVertex);
    }
    JVariantGraphEdge jvgEdge = new JVariantGraphEdge(lastJvgVertex, jvgVertex, vgEdge);
    joinedGraph.addEdge(lastJvgVertex, jvgVertex, jvgEdge);
    if (vgVertexIsNew) {
      checkNextVertex(vgVertex, jvgVertex);
    }
  }

  private void checkNextVertex(IVariantGraphVertex vgVertex, IJVariantGraphVertex jvgVertex) {
    Set<IVariantGraphEdge> outgoingEdges = unjoinedGraph.outgoingEdgesOf(vgVertex);
    if (outgoingEdges.size() == 1) {
      IVariantGraphVertex targetVgVertex = unjoinedGraph.getEdgeTarget(outgoingEdges.iterator().next());
      if (vertexHasOneIncomingEdge(targetVgVertex) && vertexHasOutgoingEdges(targetVgVertex)) {
        if (!vertexMap.containsKey(targetVgVertex)) {
          jvgVertex.addVariantGraphVertex(targetVgVertex);
        }
        vertexMap.put(targetVgVertex, jvgVertex);
        checkNextVertex(targetVgVertex, jvgVertex);
      } else {
        processEdge(outgoingEdges.iterator().next(), jvgVertex);
      }
    } else {
      for (IVariantGraphEdge vgEdge : outgoingEdges) {
        processEdge(vgEdge, jvgVertex);
      }
    }
  }

  private boolean vertexHasOneIncomingEdge(final IVariantGraphVertex vertex) {
    return unjoinedGraph.inDegreeOf(vertex) == 1;
  }

  private boolean vertexHasOutgoingEdges(IVariantGraphVertex vertex) {
    return unjoinedGraph.outDegreeOf(vertex) > 0;
  }

}
