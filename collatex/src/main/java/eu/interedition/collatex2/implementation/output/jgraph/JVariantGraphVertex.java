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

package eu.interedition.collatex2.implementation.output.jgraph;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.nonpublic.joined_graph.IJVariantGraphVertex;

public class JVariantGraphVertex implements IJVariantGraphVertex {
  private final StringBuilder normalized;
  private final Set<IWitness> witnesses;
  private final List<IVariantGraphVertex> variantGraphVertices;

  public JVariantGraphVertex(IVariantGraphVertex vgVertex) {
    normalized = new StringBuilder(vgVertex.getNormalized());
    witnesses = vgVertex.getWitnesses();
    variantGraphVertices = Lists.newArrayList(vgVertex);
  }

  public JVariantGraphVertex(String normalizedToken) {
    normalized = new StringBuilder(normalizedToken);
    witnesses = Sets.newHashSet();
    //NOTE: this could be dangerous! Where is this constructor called?
    variantGraphVertices = Lists.newArrayList();
  }

  @Override
  public void addVariantGraphVertex(IVariantGraphVertex nextVertex) {
    variantGraphVertices.add(nextVertex);
    normalized.append(" ").append(nextVertex.getNormalized());
  }

  @Override
  public String getNormalized() {
    return normalized.toString();
  }

  @Override
  public Set<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public String toString() {
    return "{" + getNormalized() + "}";
  }

  @Override
  public List<IVariantGraphVertex> getVariantGraphVertices() {
    return variantGraphVertices;
  }
}
