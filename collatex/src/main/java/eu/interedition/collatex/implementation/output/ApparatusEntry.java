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

import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.graph.SegmentedVariantGraphVertex;
import eu.interedition.collatex.interfaces.ApparatusEntryState;
import eu.interedition.collatex.interfaces.IApparatusEntry;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ApparatusEntry implements IApparatusEntry {

  private final List<IWitness> witnesses;
  private final Set<SegmentedVariantGraphVertex> vertices;
  
  public ApparatusEntry(List<IWitness> witnesses) {
    this.witnesses = witnesses;
    this.vertices = Sets.newLinkedHashSet();
  }
  
  public void addVertex(SegmentedVariantGraphVertex vertex) {
    vertices.add(vertex);
  }

  @Override
  public List<IWitness> getWitnesses() {
    return witnesses;
  }

  //This means that a reading is not empty!
  //TODO: rename!
  @Override
  public boolean containsWitness(IWitness witness) {
    SegmentedVariantGraphVertex result = null;
    for (SegmentedVariantGraphVertex vertex : vertices) {
      if (vertex.containsWitness(witness)) {
        result = vertex;
        break;
      }
    }
    return result != null;
  }
  
  //Note: an empty cell returns an empty phrase!
  @Override
  public List<INormalizedToken> getPhrase(final IWitness witness) {
    SegmentedVariantGraphVertex result = null;
    for (SegmentedVariantGraphVertex vertex : vertices) {
      if (vertex.containsWitness(witness)) {
        result = vertex;
        break;
      }
    }
    return (result == null ? Collections.<INormalizedToken>emptyList() : result.getPhrase(witness));
  }

  @Override
  public boolean hasEmptyCells() {
    int nonEmptyWitnessSize = 0;
    for (SegmentedVariantGraphVertex vertex : vertices) {
      nonEmptyWitnessSize += vertex.getWitnesses().size();
    }
    return getWitnesses().size() != nonEmptyWitnessSize;
  }

  @Override
  public ApparatusEntryState getState() {
    int size = vertices.size();
    if (size == 1) {
      boolean emptyCells = hasEmptyCells();
      if (!emptyCells) {
        return ApparatusEntryState.INVARIANT;
      } 
      return ApparatusEntryState.SEMI_INVARIANT;  
    }
    return ApparatusEntryState.VARIANT;
  }
}
