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

import com.google.common.collect.Maps;
import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IVariantGraphEdge;
import eu.interedition.collatex.interfaces.IVariantGraphVertex;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.util.Map;
import java.util.Set;

public class JoinedVariantGraph extends DirectedAcyclicGraph<JoinedVariantGraphVertex, JoinedVariantGraphEdge> {
  private JoinedVariantGraphVertex start;
  private JoinedVariantGraphVertex end;

  public static JoinedVariantGraph create(final IVariantGraph source) {
    final IVariantGraphVertex sourceStart = source.getStartVertex();
    final IVariantGraphVertex sourceEnd = source.getEndVertex();

    final JoinedVariantGraph target = new JoinedVariantGraph(sourceStart, sourceEnd);
    final Map<IVariantGraphVertex, JoinedVariantGraphVertex> created = Maps.newHashMap();
    created.put(sourceStart, target.getStart());
    created.put(sourceEnd, target.getEnd());

    for (IVariantGraphEdge unjoined : source.outgoingEdgesOf(sourceStart)) {
      convert(source, target, created, unjoined, target.getStart());
    }

    return target;
  }

  private JoinedVariantGraph(IVariantGraphVertex sourceStart, IVariantGraphVertex sourceEnd) {
    super(JoinedVariantGraphEdge.class);
    addVertex(this.start = new JoinedVariantGraphVertex(sourceStart));
    addVertex(this.end = new JoinedVariantGraphVertex(sourceEnd));
  }

  public JoinedVariantGraphVertex getStart() {
    return start;
  }

  public JoinedVariantGraphVertex getEnd() {
    return end;
  }

  private static void convert(IVariantGraph source, JoinedVariantGraph target, Map<IVariantGraphVertex, JoinedVariantGraphVertex> created, IVariantGraphEdge unjoined, JoinedVariantGraphVertex lastJoinedVertex) {
    IVariantGraphVertex unjoinedTarget = source.getEdgeTarget(unjoined);
    JoinedVariantGraphVertex joined;
    boolean unjoinedIsNew = true;
    if (created.containsKey(unjoinedTarget)) {
      joined = created.get(unjoinedTarget);
      unjoinedIsNew = false;
    } else {
      joined = new JoinedVariantGraphVertex(unjoinedTarget);
      created.put(unjoinedTarget, joined);
      target.addVertex(joined);
    }
    target.addEdge(lastJoinedVertex, joined, new JoinedVariantGraphEdge(lastJoinedVertex, joined, unjoined));
    if (unjoinedIsNew) {
      convert(source, target, created, unjoinedTarget, joined);
    }
  }

  private static void convert(IVariantGraph source, JoinedVariantGraph target, Map<IVariantGraphVertex, JoinedVariantGraphVertex> created, IVariantGraphVertex unjoined, JoinedVariantGraphVertex joined) {
    final Set<IVariantGraphEdge> unjoinedOutgoing = source.outgoingEdgesOf(unjoined);
    if (unjoinedOutgoing.size() == 1) {
      IVariantGraphVertex unjoinedTarget = source.getEdgeTarget(unjoinedOutgoing.iterator().next());
      if (source.inDegreeOf(unjoinedTarget) == 1 && source.outDegreeOf(unjoinedTarget) > 0) {
        if (!created.containsKey(unjoinedTarget)) {
          joined.add(unjoinedTarget);
        }
        created.put(unjoinedTarget, joined);
        convert(source, target, created, unjoinedTarget, joined);
      } else {
        convert(source, target, created, unjoinedOutgoing.iterator().next(), joined);
      }
    } else {
      for (IVariantGraphEdge unjoinedEdge : unjoinedOutgoing) {
        convert(source, target, created, unjoinedEdge, joined);
      }
    }
  }
}
