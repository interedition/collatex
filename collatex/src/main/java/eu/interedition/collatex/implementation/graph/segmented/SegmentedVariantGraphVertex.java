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

package eu.interedition.collatex.implementation.graph.segmented;

import com.google.common.collect.Lists;
import eu.interedition.collatex.implementation.input.NormalizedToken;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.List;
import java.util.Map;

public class SegmentedVariantGraphVertex implements
    ISegmentedVariantGraphVertex {

  private final Map<IWitness, List<INormalizedToken>> phraseForEachWitness;

  public SegmentedVariantGraphVertex(Map<IWitness, List<INormalizedToken>> phraseForEachWitness) {
    this.phraseForEachWitness = phraseForEachWitness;
  }

  @Override
  public String getNormalized() {
    if (phraseForEachWitness.isEmpty()) {
      return "#";
    }
    return NormalizedToken.toString(phraseForEachWitness.values().iterator().next());
  }

  //NOTE: should this be a list?
  @Override
  public List<IWitness> getWitnesses() {
    return Lists.newArrayList(phraseForEachWitness.keySet());
  }

  @Override
  public List<INormalizedToken> getPhrase(IWitness witness) {
    return phraseForEachWitness.get(witness);
  }

  @Override
  public boolean containsWitness(IWitness witness) {
    return phraseForEachWitness.containsKey(witness);
  }
 
}
