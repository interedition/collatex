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

package eu.interedition.collatex2.implementation.containers.graph;

import java.util.Collections;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IWitness;

@SuppressWarnings("serial")
public class VariantGraphEdge extends DefaultWeightedEdge implements IVariantGraphEdge {
  private final Set<IWitness> witnesses;

  public VariantGraphEdge() {
    this.witnesses = Sets.newLinkedHashSet();
  }

  @Override
  public Set<IWitness> getWitnesses() {
    return Collections.unmodifiableSet(witnesses);
  }

  @Override
  public String toString() {
    String splitter = "";
    StringBuilder to = new StringBuilder().append(": ");
    for (IWitness witness : witnesses) {
      to.append(splitter).append(witness.getSigil());
      splitter = ", ";
    }
    return to.toString();
  }

  @Override
  public void addWitness(IWitness witness) {
    witnesses.add(witness);
  }

  @Override
  public boolean containsWitness(IWitness witness) {
    return witnesses.contains(witness);
  }

}
