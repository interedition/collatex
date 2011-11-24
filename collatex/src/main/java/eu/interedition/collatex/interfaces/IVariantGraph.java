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

package eu.interedition.collatex.interfaces;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;


public interface IVariantGraph extends DirectedGraph<IVariantGraphVertex, IVariantGraphEdge>, ITokenContainer, Iterable<IVariantGraphVertex> {

  IVariantGraphVertex getStartVertex();

  IVariantGraphVertex getEndVertex();

  // Iterates over vertices in topological order
  Iterator<IVariantGraphVertex> iterator();

  List<IWitness> getWitnesses();

  boolean isEmpty();

  List<INormalizedToken> getTokens(IWitness witness);

  List<IVariantGraphEdge> getPath(IWitness witness);
  
  Map<IVariantGraphVertex, IVariantGraphVertex> getTransposedTokens();

}