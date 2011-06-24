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

package eu.interedition.collatex2.implementation.output.cgraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.interedition.collatex2.implementation.vg_alignment.VariantGraphEdge;
import eu.interedition.collatex2.implementation.vg_alignment.VariantGraphVertex;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

@SuppressWarnings("serial")
public class CyclicVariantGraph extends SimpleDirectedGraph<IVariantGraphVertex, IVariantGraphEdge> implements IVariantGraph {
  private final IVariantGraphVertex startVertex;
  private final IVariantGraphVertex endVertex;

  private CyclicVariantGraph() {
    super(IVariantGraphEdge.class);
    startVertex = new VariantGraphVertex("#", null);
    addVertex(startVertex);
    endVertex = new VariantGraphVertex("#", null);
    addVertex(endVertex);
  }

  public static CyclicVariantGraph create() {
    return new CyclicVariantGraph();
  }

  public static CyclicVariantGraph create(IWitness a) {
    CyclicVariantGraph graph = CyclicVariantGraph.create();
    List<IVariantGraphVertex> newVertices = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      final IVariantGraphVertex vertex = graph.addNewVertex(token.getNormalized(), token);
      vertex.addToken(a, token);
      newVertices.add(vertex);
    }
    IVariantGraphVertex previous = graph.getStartVertex();
    for (IVariantGraphVertex vertex : newVertices) {
      graph.addNewEdge(previous, vertex, a);
      previous = vertex;
    }
    graph.addNewEdge(previous, graph.getEndVertex(), a);
    return graph;
  }

  //write
  public IVariantGraphVertex addNewVertex(String normalized, INormalizedToken vertexKey) {
    final VariantGraphVertex vertex = new VariantGraphVertex(normalized, vertexKey);
    addVertex(vertex);
    return vertex;
  }

  //write
  public void addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    IVariantGraphEdge edge = new VariantGraphEdge();
    edge.addWitness(witness);
    addEdge(begin, end, edge);
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
    throw new UnsupportedOperationException("NOT IMPLEMENTED!");
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException("NOT IMPLEMENTED!");
  }

  @Override
  public List<IVariantGraphEdge> getPath(IWitness witness) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED!");
  }

  @Override
  public List<INormalizedToken> getTokens(IWitness witness) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED!");
  }

  @Override
  public boolean isNear(IToken a, IToken b) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED!");
  }

  @Override
  public Iterator<INormalizedToken> tokenIterator() {
    throw new UnsupportedOperationException("NOT IMPLEMENTED!");
  }

  @Override
  public Iterator<IVariantGraphVertex> iterator() {
    throw new UnsupportedOperationException("NOT IMPLEMENTED!");
  }
  
  @Override
  public Map<IVariantGraphVertex, IVariantGraphVertex> getTransposedTokens() {
	  throw new UnsupportedOperationException("Cyclic graphs have no transpositions");
  }

  @Override
  public IVariantGraph add(IWitness witness) {
    throw new UnsupportedOperationException("NOT IMPLEMENTED!");
  }
}
