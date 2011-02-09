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

package eu.interedition.collatex2.implementation.output.apparatus;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraphVertex;
import eu.interedition.collatex2.interfaces.ApparatusEntryState;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: Extract interface for this class! Do not expose Vertex in the interface!
public class ApparatusEntry {

  private final List<IWitness> witnesses;
  private final Set<ISegmentedVariantGraphVertex> vertices;
  
  public ApparatusEntry(List<IWitness> witnesses) {
    this.witnesses = witnesses;
    this.vertices = Sets.newLinkedHashSet();
  }
  
  public void addVertex(ISegmentedVariantGraphVertex vertex) {
    vertices.add(vertex);
  }

  public List<IWitness> getWitnesses() {
    return witnesses;
  }

  //This means that a reading is not empty!
  //TODO: rename!
  public boolean containsWitness(IWitness witness) {
    ISegmentedVariantGraphVertex result = null;
    for (ISegmentedVariantGraphVertex vertex : vertices) {
      if (vertex.containsWitness(witness)) {
        result = vertex;
        break;
      }
    }
    return result != null;
  }
  
  //Note: an empty cell returns an empty phrase!
  @SuppressWarnings("unchecked")
  public IPhrase getPhrase(final IWitness witness) {
    ISegmentedVariantGraphVertex result = null;
    for (ISegmentedVariantGraphVertex vertex : vertices) {
      if (vertex.containsWitness(witness)) {
        result = vertex;
        break;
      }
    }
    if (result == null) {
      return new Phrase(Collections.EMPTY_LIST);
    }  
    return result.getPhrase(witness);
  }

  public boolean hasEmptyCells() {
    int nonEmptyWitnessSize = 0;
    for (ISegmentedVariantGraphVertex vertex : vertices) {
      nonEmptyWitnessSize += vertex.getWitnesses().size();
    }
    return getWitnesses().size() != nonEmptyWitnessSize;
  }

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
