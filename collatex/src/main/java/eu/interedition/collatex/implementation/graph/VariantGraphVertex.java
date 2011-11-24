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

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IVariantGraphVertex;
import eu.interedition.collatex.interfaces.IWitness;

public class VariantGraphVertex implements IVariantGraphVertex {
  private final String normalized;
  private final Map<IWitness, INormalizedToken> tokenMap;
  private final INormalizedToken vertexKey;

  public VariantGraphVertex(String normalized, INormalizedToken vertexKey) {
    this.normalized = normalized;
    this.vertexKey = vertexKey;
    this.tokenMap = Maps.newLinkedHashMap();
  }

  @Override
  public String getNormalized() {
    return normalized;
  }

  @Override
  public INormalizedToken getToken(IWitness witness) {
    if (!tokenMap.containsKey(witness)) {
      throw new RuntimeException("TOKEN FOR WITNESS " + witness.getSigil() + " NOT FOUND IN VERTEX " + getNormalized() + "!");
    }
    return tokenMap.get(witness);
  }

  @Override
  public void addToken(IWitness witness, INormalizedToken token) {
    tokenMap.put(witness, token);
  }

  @Override
  public boolean containsWitness(IWitness witness) {
    return tokenMap.containsKey(witness);
  }

  @Override
  public Set<IWitness> getWitnesses() {
    return tokenMap.keySet();
  }

  @Override
  public String toString() {
    return "[" + getNormalized() + "]";
  }

  @Override
  public String getContent() {
    throw new RuntimeException("Do not call this method! Call getToken(IWitness).getContent() instead.");
  }

  public INormalizedToken getVertexKey() {
    return vertexKey;
  }

  @Override
  public String getTrailingWhitespace() {
    throw new RuntimeException("Do not call this method! Call getToken(IWitness).getTrailingWhitespace() instead.");
  }

}
