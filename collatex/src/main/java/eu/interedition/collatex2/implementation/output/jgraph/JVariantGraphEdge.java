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

import java.util.Set;

import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.nonpublic.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.nonpublic.IJVariantGraphVertex;

public class JVariantGraphEdge implements IJVariantGraphEdge {
  private final Set<IWitness> witnesses;
  private final IJVariantGraphVertex beginVertex;
  private final IJVariantGraphVertex endVertex;

  public JVariantGraphEdge(IJVariantGraphVertex beginVertex, IJVariantGraphVertex endVertex, IVariantGraphEdge vgEdge) {
    this.beginVertex = beginVertex;
    this.endVertex = endVertex;
    witnesses = vgEdge.getWitnesses();
  }

  @Override
  public Set<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public String toString() {
    return beginVertex + " --{" + witnesses + "}-> " + endVertex;
  }

}
