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

package eu.interedition.collatex2.implementation.graph.cyclic;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.graph.VariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class CVariantGraphCreator {
  //private static final Logger LOG = LoggerFactory.getLogger(CVariantGraphCreator.class);

  public static IVariantGraph getCyclicVariantGraph(final IVariantGraph acyclicGraph) {
    Map<IVariantGraphVertex, IVariantGraphVertex> a2cVertexMap = Maps.newHashMap(); // maps vertex in acyclic graph to vertex in cyclic graph
    IVariantGraph cyclicGraph = CyclicVariantGraph.create();
    
    a2cVertexMap.put(acyclicGraph.getStartVertex(), cyclicGraph.getStartVertex());
    a2cVertexMap.put(acyclicGraph.getEndVertex(), cyclicGraph.getEndVertex());
        
    Map<IVariantGraphVertex, IVariantGraphVertex> transposedVertices = acyclicGraph.getTransposedTokens();
    for (IVariantGraphVertex avgVertex : acyclicGraph.vertexSet()) {
      if (!a2cVertexMap.containsKey(avgVertex)) {
        IVariantGraphVertex cvgVertex;
        if (transposedVertices.containsKey(avgVertex)) {
        	IVariantGraphVertex txpVertex = transposedVertices.get(avgVertex);
        	if(a2cVertexMap.containsKey(txpVertex)) {
        		cvgVertex = a2cVertexMap.get(txpVertex);
        	} else {
        		cvgVertex = new VariantGraphVertex(txpVertex.getNormalized(), txpVertex.getVertexKey());
        		cyclicGraph.addVertex(cvgVertex);
        	}
        } else {
          cvgVertex = new VariantGraphVertex(avgVertex.getNormalized(), avgVertex.getVertexKey());
          cyclicGraph.addVertex(cvgVertex);
        }
        a2cVertexMap.put(avgVertex, cvgVertex);
      }
    }

    for (IVariantGraphEdge avgEdge : acyclicGraph.edgeSet()) {
      IVariantGraphVertex cvgStart = a2cVertexMap.get(acyclicGraph.getEdgeSource(avgEdge));
      IVariantGraphVertex cvgEnd = a2cVertexMap.get(acyclicGraph.getEdgeTarget(avgEdge));
      Iterator<IWitness> witnessIterator = avgEdge.getWitnesses().iterator();
      IVariantGraphEdge cvgEdge;
      if (cyclicGraph.containsEdge(cvgStart, cvgEnd)) {
        cvgEdge = cyclicGraph.getEdge(cvgStart, cvgEnd);
      } else {
        cvgEdge = new VariantGraphEdge();
        cvgEdge.addWitness(witnessIterator.next());
        cyclicGraph.addEdge(cvgStart, cvgEnd, cvgEdge);
      }
      while (witnessIterator.hasNext()) {
        cvgEdge.addWitness(witnessIterator.next());
      }
    }

    return cyclicGraph;
  }

}
