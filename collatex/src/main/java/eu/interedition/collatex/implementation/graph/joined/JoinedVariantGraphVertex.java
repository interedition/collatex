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

package eu.interedition.collatex.implementation.graph.joined;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.interfaces.IVariantGraphVertex;
import eu.interedition.collatex.interfaces.IWitness;

public class JoinedVariantGraphVertex {
  private final StringBuilder normalized;
  private final Set<IWitness> witnesses;
  private final List<IVariantGraphVertex> sources;

  public JoinedVariantGraphVertex(IVariantGraphVertex source) {
    this.normalized = new StringBuilder(source.getNormalized());
    this.witnesses = source.getWitnesses();
    this.sources = Lists.newArrayList(source);
  }

  public String getNormalized() {
    return normalized.toString();
  }

  public Set<IWitness> getWitnesses() {
    return witnesses;
  }

  public List<IVariantGraphVertex> getSources() {
    return sources;
  }

  public void add(IVariantGraphVertex source) {
    this.sources.add(source);
    this.normalized.append(" ").append(source.getNormalized());
  }

  @Override
  public String toString() {
    return new StringBuilder().append("{").append(getNormalized()).append("}").toString();
  }
}
