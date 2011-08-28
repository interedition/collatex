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

import java.util.List;

import eu.interedition.collatex2.implementation.vg_alignment.AbstractTokenIndex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphIndex extends AbstractTokenIndex {

  public VariantGraphIndex(IVariantGraph graph, List<String> repeatingTokens) {
    super();
    for (IWitness witness: graph.getWitnesses()) {
      List<INormalizedToken> tokens = graph.getTokens(witness);
      processTokens(tokens, repeatingTokens);
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("VariantGraphIndex: (");
    String delimiter = "";
    for (final String normalizedPhrase : keys()) {
      result.append(delimiter).append(normalizedPhrase);
      delimiter = ", ";
    }
    result.append(")");
    return result.toString();
  }


}
