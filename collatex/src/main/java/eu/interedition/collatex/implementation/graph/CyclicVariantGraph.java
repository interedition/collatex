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

package eu.interedition.collatex.implementation.graph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import org.jgrapht.graph.SimpleDirectedGraph;

import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IToken;
import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IVariantGraphEdge;
import eu.interedition.collatex.interfaces.IVariantGraphVertex;
import eu.interedition.collatex.interfaces.IWitness;

@SuppressWarnings("serial")
public class CyclicVariantGraph extends SimpleDirectedGraph<IVariantGraphVertex, IVariantGraphEdge> implements IVariantGraph {
  private final IVariantGraphVertex startVertex;
  private final IVariantGraphVertex endVertex;

  public static IVariantGraph create(IVariantGraph acyclic) {
    // maps vertex in acyclic graph to vertex in cyclic graph
    final Map<IVariantGraphVertex, IVariantGraphVertex> vertexMap = Maps.newHashMap();
    final IVariantGraph cyclic = new CyclicVariantGraph();

    vertexMap.put(acyclic.getStartVertex(), cyclic.getStartVertex());
    vertexMap.put(acyclic.getEndVertex(), cyclic.getEndVertex());

    final Map<IVariantGraphVertex, IVariantGraphVertex> transposedVertices = acyclic.getTransposedTokens();
    for (IVariantGraphVertex avgVertex : acyclic.vertexSet()) {
      if (!vertexMap.containsKey(avgVertex)) {
        IVariantGraphVertex cvgVertex;
        if (transposedVertices.containsKey(avgVertex)) {
        	IVariantGraphVertex txpVertex = transposedVertices.get(avgVertex);
        	if(vertexMap.containsKey(txpVertex)) {
        		cvgVertex = vertexMap.get(txpVertex);
        	} else {
        		cvgVertex = new VariantGraphVertex(txpVertex.getNormalized(), txpVertex.getVertexKey());
        		cyclic.addVertex(cvgVertex);
        	}
        } else {
          cvgVertex = new VariantGraphVertex(avgVertex.getNormalized(), avgVertex.getVertexKey());
          cyclic.addVertex(cvgVertex);
        }
        vertexMap.put(avgVertex, cvgVertex);
      }
    }

    for (IVariantGraphEdge avgEdge : acyclic.edgeSet()) {
      IVariantGraphVertex cvgStart = vertexMap.get(acyclic.getEdgeSource(avgEdge));
      IVariantGraphVertex cvgEnd = vertexMap.get(acyclic.getEdgeTarget(avgEdge));
      Iterator<IWitness> witnessIterator = avgEdge.getWitnesses().iterator();
      IVariantGraphEdge cvgEdge;
      if (cyclic.containsEdge(cvgStart, cvgEnd)) {
        cvgEdge = cyclic.getEdge(cvgStart, cvgEnd);
      } else {
        cvgEdge = new VariantGraphEdge();
        cvgEdge.addWitness(witnessIterator.next());
        cyclic.addEdge(cvgStart, cvgEnd, cvgEdge);
      }
      while (witnessIterator.hasNext()) {
        cvgEdge.addWitness(witnessIterator.next());
      }
    }

    return cyclic;
  }

  private CyclicVariantGraph() {
    super(IVariantGraphEdge.class);
    startVertex = new VariantGraphVertex("#", null);
    addVertex(startVertex);
    endVertex = new VariantGraphVertex("#", null);
    addVertex(endVertex);
  }

  @Override
  public IVariantGraphVertex getEndVertex() {
    return endVertex;
  }

  @Override
  public IVariantGraphVertex getStartVertex() {
    return startVertex;
  }

  @Override
  public List<IWitness> getWitnesses() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<IVariantGraphEdge> getPath(IWitness witness) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<INormalizedToken> getTokens(IWitness witness) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isNear(IToken a, IToken b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<INormalizedToken> tokenIterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<IVariantGraphVertex> iterator() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Map<IVariantGraphVertex, IVariantGraphVertex> getTransposedTokens() {
	  throw new UnsupportedOperationException("Cyclic graphs have no transpositions");
  }
}
