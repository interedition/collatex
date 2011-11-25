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

package eu.interedition.collatex.implementation.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraph;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraphEdge;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraphVertex;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.interedition.collatex.interfaces.IWitness;

public class JVariantGraphCreatorTest extends AbstractTest {
  private static final Logger LOG = LoggerFactory.getLogger(JVariantGraphCreatorTest.class);

  @Test
  public void joinTwoIdenticalWitnesses() {
    final IWitness[] w = createWitnesses("the black cat", "the black cat");
    final JoinedVariantGraph graph = JoinedVariantGraph.create(merge(w));
    LOG.debug("joinedGraph=" + graph);

    final JoinedVariantGraphVertex startVertex = graph.getStart();
    assertEquals("#", startVertex.getNormalized());
    assertEquals(0, startVertex.getWitnesses().size());

    final Set<JoinedVariantGraphEdge> outgoingEdges = graph.outgoingEdgesOf(startVertex);
    assertEquals(1, outgoingEdges.size());

    final JoinedVariantGraphEdge edge = outgoingEdges.iterator().next();
    final JoinedVariantGraphVertex vertex = graph.getEdgeTarget(edge);
    assertEquals("the black cat", vertex.getNormalized());

    final Set<IWitness> witnesses = edge.getWitnesses();
    assertEquals(2, witnesses.size());
    assertTrue(witnesses.contains(w[0]));
    assertTrue(witnesses.contains(w[1]));

    final List<String> phrases = extractPhrases(graph);
    assertEquals(phrases.toString(), 3, phrases.size());
    assertTrue(phrases.contains("#"));
    assertTrue(phrases.contains("the black cat"));
  }

  @Test
  public void joinTwoDifferentWitnesses() {
    final IWitness[] w = createWitnesses("the nice black cat shared his food", "the bad white cat spilled his food again");
    final JoinedVariantGraph graph = JoinedVariantGraph.create(merge(w));
    LOG.debug("joinedGraph=" + graph);

    final JoinedVariantGraphVertex startVertex = graph.getStart();
    assertEquals("#", startVertex.getNormalized());
    assertEquals(0, startVertex.getWitnesses().size());

    final Set<JoinedVariantGraphEdge> outgoingEdges = graph.outgoingEdgesOf(startVertex);
    assertEquals(1, outgoingEdges.size());

    final JoinedVariantGraphEdge edge = outgoingEdges.iterator().next();
    final JoinedVariantGraphVertex vertex = graph.getEdgeTarget(edge);
    assertEquals("the", vertex.getNormalized());

    final Set<IWitness> witnesses = edge.getWitnesses();
    assertEquals(2, witnesses.size());
    assertTrue(witnesses.contains(w[0]));
    assertTrue(witnesses.contains(w[1]));

    final List<String> phrases = extractPhrases(graph);
    assertEquals(phrases.toString(), 10, phrases.size());
    assertTrue(phrases.contains("#"));
    assertTrue(phrases.contains("the"));
    assertTrue(phrases.contains("nice black"));
    assertTrue(phrases.contains("bad white"));
    assertTrue(phrases.contains("cat"));
    assertTrue(phrases.contains("shared"));
    assertTrue(phrases.contains("spilled"));
    assertTrue(phrases.contains("his food"));
    assertTrue(phrases.contains("again"));
  }

  @Test
  public void joinTwoDifferentWitnesses2() {
    final IWitness[] w = createWitnesses("Blackie, the black cat", "Whitney, the white cat");
    final JoinedVariantGraph graph = JoinedVariantGraph.create(merge(w));
    LOG.debug("joinedGraph=" + graph);

    final JoinedVariantGraphVertex startVertex = graph.getStart();
    assertEquals("#", startVertex.getNormalized());
    assertEquals(0, startVertex.getWitnesses().size());

    final Set<JoinedVariantGraphEdge> outgoingEdges = graph.outgoingEdgesOf(startVertex);
    assertEquals(2, outgoingEdges.size());

    final Iterator<JoinedVariantGraphEdge> iterator = outgoingEdges.iterator();

    JoinedVariantGraphEdge edge = iterator.next();
    JoinedVariantGraphVertex vertex = graph.getEdgeTarget(edge);
    assertEquals("blackie", vertex.getNormalized());

    edge = iterator.next();
    vertex = graph.getEdgeTarget(edge);
    assertEquals("whitney", vertex.getNormalized());

    final Set<IWitness> witnesses = edge.getWitnesses();
    assertEquals(1, witnesses.size());
    assertTrue(witnesses.contains(w[1]));

    final List<String> phrases = extractPhrases(graph);
    assertEquals(phrases.toString(), 8, phrases.size());
    assertTrue(phrases.contains("#"));
    assertTrue(phrases.contains("blackie"));
    assertTrue(phrases.contains("whitney"));
    assertTrue(phrases.contains("the"));
    assertTrue(phrases.contains("black"));
    assertTrue(phrases.contains("white"));
    assertTrue(phrases.contains("cat"));
  }

  private static List<String> extractPhrases(JoinedVariantGraph joinedGraph) {
    Set<JoinedVariantGraphVertex> vertexSet = joinedGraph.vertexSet();
    List<String> phrases = Lists.newArrayList();
    for (JoinedVariantGraphVertex variantGraphVertex : vertexSet) {
      phrases.add(variantGraphVertex.getNormalized());
    }
    return phrases;
  }
}
