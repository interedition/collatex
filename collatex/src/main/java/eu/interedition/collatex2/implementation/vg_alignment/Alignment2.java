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

import java.util.List;

import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class Alignment2 implements IAlignment2 {

  private final IVariantGraph graph;
  private final IWitness witness;
  private final List<ITokenMatch> tokenMatches;

  public Alignment2(IVariantGraph graph, IWitness witness, List<ITokenMatch> tokenMatches) {
    this.graph = graph;
    this.witness = witness;
    this.tokenMatches = tokenMatches;
  }

  @Override
  public IVariantGraph getGraph() {
    return graph;
  }

  @Override
  public IWitness getWitness() {
    return witness;
  }

  @Override
  public List<ITokenMatch> getTokenMatches() {
    return tokenMatches;
  }
}
