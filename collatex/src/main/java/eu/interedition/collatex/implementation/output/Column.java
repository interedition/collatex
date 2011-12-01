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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import eu.interedition.collatex.interfaces.ColumnState;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

/**
 * A row of an alignment table which represents a single witness
 * <p/>
 * <p/>
 * TODO: consider whether this should be an inner interface since an IRow must exist within the context of an IAlignmentTable so the rows and columns will probably end up in the alignment table.
 */
public class Column {
  private final List<PersistentVariantGraphVertex> vertices = Lists.newArrayList();

  public Column(PersistentVariantGraphVertex vertex) {
    addVertex(vertex);
  }

  public ColumnState getState() {
    if (vertices.size() == 1) {
      return ColumnState.INVARIANT;
    }
    return ColumnState.VARIANT;
  }

  public INormalizedToken getToken(IWitness witness) {
    final SortedSet<IWitness> witnessSet = Sets.newTreeSet(Collections.singleton(witness));
    for (PersistentVariantGraphVertex vertex : vertices) {
      for (INormalizedToken token : vertex.getTokens(witnessSet)) {
        // FIXME: just picks the first of possibly several tokens per vertex
        return token;
      }
    }
    throw new NoSuchElementException("Witness " + witness.getSigil() + " is not present in this column");
  }

  //TODO: add/re-enable test (see parallel segmentation tests)
  public List<IWitness> getWitnesses() {
    List<IWitness> totalWitnesses = Lists.newArrayList();
    for (PersistentVariantGraphVertex vertex : vertices) {
      Set<IWitness> witnesses = vertex.getWitnesses();
      totalWitnesses.addAll(witnesses);
    }
    return totalWitnesses;
  }

  protected void addVertex(PersistentVariantGraphVertex vertex) {
    vertices.add(vertex);
  }

  public boolean containsWitness(IWitness witness) {
    PersistentVariantGraphVertex findVertexForWitness = findVertexForWitness(witness);
    return findVertexForWitness != null;
  }


  protected PersistentVariantGraphVertex findVertexForWitness(IWitness witness) {
    // FIXME: should maybe be a map?
    for (PersistentVariantGraphVertex vertex : vertices) {
      if (vertex.getWitnesses().contains(witness)) {
        return vertex;
      }
    }
    return null;
  }
}
