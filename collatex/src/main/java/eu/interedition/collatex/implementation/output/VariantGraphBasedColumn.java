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

package eu.interedition.collatex.implementation.output;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.interfaces.ColumnState;
import eu.interedition.collatex.interfaces.IColumn;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IVariantGraphVertex;
import eu.interedition.collatex.interfaces.IWitness;

public class VariantGraphBasedColumn implements IColumn {
  private final List<IVariantGraphVertex> vertices;

  public VariantGraphBasedColumn(IVariantGraphVertex vertex) {
    this.vertices = Lists.newArrayList();
    addVertex(vertex);
  }

  @Override
  public ColumnState getState() {
    if (vertices.size() == 1) {
      return ColumnState.INVARIANT;
    }
    return ColumnState.VARIANT;
  }

  @Override
  public INormalizedToken getToken(IWitness witness) {
    IVariantGraphVertex vertex = findVertexForWitness(witness);
    if (vertex == null) {
      throw new NoSuchElementException("Witness " + witness.getSigil() + " is not present in this column");
    }
    return vertex.getToken(witness);
  }

  //TODO: add/re-enable test (see parallel segmentation tests)
  @Override
  public List<IWitness> getWitnesses() {
    List<IWitness> totalWitnesses = Lists.newArrayList();
    for (IVariantGraphVertex vertex : vertices) {
      Set<IWitness> witnesses = vertex.getWitnesses();
      totalWitnesses.addAll(witnesses);
    }
    return totalWitnesses;
  }

  protected void addVertex(IVariantGraphVertex vertex) {
    vertices.add(vertex);
  }

  @Override
  public boolean containsWitness(IWitness witness) {
    IVariantGraphVertex findVertexForWitness = findVertexForWitness(witness);
    return findVertexForWitness != null;
  }


  // should maybe be a map?
  protected IVariantGraphVertex findVertexForWitness(IWitness witness) {
    for (IVariantGraphVertex vertex : vertices) {
      if (vertex.containsWitness(witness)) {
        return vertex;
      }
    }
    return null;
  }

}
