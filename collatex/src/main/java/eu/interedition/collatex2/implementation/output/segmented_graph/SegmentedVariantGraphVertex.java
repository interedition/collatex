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

package eu.interedition.collatex2.implementation.output.segmented_graph;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

public class SegmentedVariantGraphVertex implements
    ISegmentedVariantGraphVertex {

  private final Map<IWitness, IPhrase> phraseForEachWitness;

  public SegmentedVariantGraphVertex(Map<IWitness, IPhrase> phraseForEachWitness) {
    this.phraseForEachWitness = phraseForEachWitness;
  }

  @Override
  public String getNormalized() {
    if (phraseForEachWitness.isEmpty()) {
      return "#";
    }
    return phraseForEachWitness.values().iterator().next().getNormalized();
  }

  //NOTE: should this be a list?
  @Override
  public List<IWitness> getWitnesses() {
    return Lists.newArrayList(phraseForEachWitness.keySet());
  }

  @Override
  public IPhrase getPhrase(IWitness witness) {
    return phraseForEachWitness.get(witness);
  }

  @Override
  public boolean containsWitness(IWitness witness) {
    return phraseForEachWitness.containsKey(witness);
  }
 
}
