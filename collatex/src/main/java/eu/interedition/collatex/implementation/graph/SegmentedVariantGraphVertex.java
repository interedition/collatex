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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.input.NormalizedToken;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.*;

public class SegmentedVariantGraphVertex {

  private final SortedSet<IWitness> witnesses = Sets.newTreeSet();
  private final Map<IWitness, List<INormalizedToken>> phrases;

  public SegmentedVariantGraphVertex(Map<IWitness, List<INormalizedToken>> phrases) {
    this.phrases = phrases;
    this.witnesses.addAll(phrases.keySet());
  }

  public String getNormalized() {
    if (phrases.isEmpty()) {
      return "#";
    }
    return NormalizedToken.toString(phrases.values().iterator().next());
  }

  public SortedSet<IWitness> getWitnesses() {
    return Collections.unmodifiableSortedSet(witnesses);
  }

  public List<INormalizedToken> getPhrase(IWitness witness) {
    return phrases.get(witness);
  }

  public boolean containsWitness(IWitness witness) {
    return witnesses.contains(witness);
  }
 
}
