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

package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class TokenIndexUtil {

  public static List<String> getRepeatedTokens(IVariantGraph graph) {
    // remove start and end vertices
    Set<IVariantGraphVertex> copy = Sets.newLinkedHashSet(graph.vertexSet());
    copy.remove(graph.getStartVertex());
    copy.remove(graph.getEndVertex());
    // we map all vertices to their normalized version
    Multimap<String, IVariantGraphVertex> mapped = ArrayListMultimap.create();
    for (IVariantGraphVertex v : copy) {
      mapped.put(v.getNormalized(), v);
    }
    // fetch all the duplicate keys and return them 
    List<String> result = Lists.newArrayList();
    for (String key : mapped.keySet()) {
      if (mapped.get(key).size() > 1) {
        result.add(key);
      }
    }
    return result;
  }

  public static List<String> getRepeatedTokens(IWitness witness) {
    final Multimap<String, INormalizedToken> normalizedTokenMap = ArrayListMultimap.create();
    for (final INormalizedToken token : witness.getTokens()) {
      normalizedTokenMap.put(token.getNormalized(), token);
    }
    final List<String> repeatingNormalizedTokens = Lists.newArrayList();
    for (final String key : normalizedTokenMap.keySet()) {
      final Collection<INormalizedToken> tokenCollection = normalizedTokenMap.get(key);
      if (tokenCollection.size() > 1) {
        repeatingNormalizedTokens.add(key);
      }
    }
    return repeatingNormalizedTokens;
  }

}
