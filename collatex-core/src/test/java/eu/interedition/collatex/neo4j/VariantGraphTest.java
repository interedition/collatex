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

package eu.interedition.collatex.neo4j;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import eu.interedition.collatex.simple.SimpleWitness;

public class VariantGraphTest extends AbstractTest {

  @Test
  public void emptyGraph() {
    final Neo4jVariantGraph graph = collate(createWitnesses());
    assertEquals(0, graph.witnesses().size());
    assertEquals(2, Iterables.size(graph.vertices()));
    assertEquals(1, Iterables.size(graph.edges()));
  }

  @Test
  public void reconnectingVerticesYieldsSameEdge() {
    final SimpleWitness witness = createWitnesses("hello world")[0];
    final Neo4jVariantGraph graph = graphFactory.newVariantGraph();
    final Neo4jVariantGraphVertex helloVertex = graph.add(witness.getTokens().get(0));
    final Neo4jVariantGraphVertex worldVertex = graph.add(witness.getTokens().get(1));
    final Neo4jVariantGraphEdge edge = graph.connect(helloVertex, worldVertex, Collections.<Witness> singleton(witness));

    Assert.assertEquals(1, edge.witnesses().size());

    Assert.assertEquals(edge, graph.connect(helloVertex, worldVertex, Collections.<Witness> singleton(witness)));
    Assert.assertEquals(1, edge.witnesses().size());
  }

  @Test
  public void getTokens() {
    final SimpleWitness[] w = createWitnesses("a b c d");
    final Neo4jVariantGraph graph = collate(w);
    final List<Neo4jVariantGraphVertex> vertices = Lists.newArrayList(graph.vertices(Sets.newHashSet(Arrays.<Witness> asList(w))));
    assertEquals(6, vertices.size());
    assertEquals(graph.getStart(), vertices.get(0));
    assertVertexEquals("a", vertices.get(1));
    assertVertexEquals("b", vertices.get(2));
    assertVertexEquals("c", vertices.get(3));
    assertVertexEquals("d", vertices.get(4));
    assertEquals(graph.getEnd(), vertices.get(5));
  }

  @Test
  public void oneWitness() {
    final SimpleWitness[] w = createWitnesses("only one witness");
    final Neo4jVariantGraph graph = collate(w);

    assertEquals(5, Iterables.size(graph.vertices()));
    assertEquals(4, Iterables.size(graph.edges()));

    final Neo4jVariantGraphVertex firstVertex = vertexWith(graph, "only", w[0]);
    final Neo4jVariantGraphVertex secondVertex = vertexWith(graph, "one", w[0]);
    final Neo4jVariantGraphVertex thirdVertex = vertexWith(graph, "witness", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), firstVertex), w[0]);
    assertHasWitnesses(edgeBetween(firstVertex, secondVertex), w[0]);
    assertHasWitnesses(edgeBetween(secondVertex, thirdVertex), w[0]);
    assertHasWitnesses(edgeBetween(thirdVertex, graph.getEnd()), w[0]);
  }

  @Test
  public void getPathForWitness() {
    final SimpleWitness[] w = createWitnesses("a b c d e f ", "x y z d e", "a b x y z");
    final Neo4jVariantGraph graph = collate(w);
    final Set<Witness> witnessSet = Collections.<Witness> singleton(w[0]);
    final List<Neo4jVariantGraphVertex> path = Lists.newArrayList(graph.vertices(witnessSet));

    assertEquals(8, path.size());
    assertEquals(graph.getStart(), path.get(0));
    assertVertexEquals("a", path.get(1));
    assertVertexEquals("b", path.get(2));
    assertVertexEquals("c", path.get(3));
    assertVertexEquals("d", path.get(4));
    assertVertexEquals("e", path.get(5));
    assertVertexEquals("f", path.get(6));
    assertEquals(graph.getEnd(), path.get(7));
  }

  @Test
  public void transpositions() {
    final SimpleWitness[] w = createWitnesses("the black and white cat", "the white and black cat", "the black and black cat");
    final Neo4jVariantGraph graph = collate(w[0], w[1]);

    assertEquals(2, graph.transpositions().size());

    collate(graph, w[2]);
    final Set<Neo4jVariantGraphTransposition> transposed = graph.transpositions();
    assertEquals(2, transposed.size());
  }

  @Test
  public void transpositions1() {
    final Neo4jVariantGraph graph = collate("the nice black and white cat", "the friendly white and black cat");
    assertEquals(12, Iterables.size(graph.edges()));
    assertEquals(12, Iterables.size(graph.edges()));
  }

  @Test
  public void transpositions2() {
    final SimpleWitness[] w = createWitnesses("The black dog chases a red cat.", "A red cat chases the black dog.", "A red cat chases the yellow dog");
    final Neo4jVariantGraph graph = collate(w);

    // There should be two vertices for cat in the graph
    Neo4jVariantGraphEdge edge = edgeBetween(vertexWith(graph, "red", w[0]), vertexWith(graph, "cat", w[0]));
    assertHasWitnesses(edge, w[0]);
    edge = edgeBetween(vertexWith(graph, "red", w[1]), vertexWith(graph, "cat", w[1]));
    assertHasWitnesses(edge, w[1], w[2]);

    assertEquals(16, Iterables.size(graph.vertices())); // start and end vertices included
    assertEquals(18, Iterables.size(graph.edges()));
  }

  @Test
  public void joinTwoIdenticalWitnesses() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the black cat");
    final Neo4jVariantGraph graph = collate(w).join();

    assertEquals(3, Iterables.size(graph.vertices()));
    assertEquals(2, Iterables.size(graph.edges()));

    final Neo4jVariantGraphVertex joinedVertex = vertexWith(graph, "the black cat", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), joinedVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(graph.getEnd(), joinedVertex), w[0], w[1]);
  }

  @Test
  public void joinTwoDifferentWitnesses() {
    final SimpleWitness[] w = createWitnesses("the nice black cat shared his food", "the bad white cat spilled his food again");
    final Neo4jVariantGraph graph = collate(w).join();

    final Neo4jVariantGraphVertex theVertex = vertexWith(graph, "the", w[0]);
    final Neo4jVariantGraphVertex niceBlackVertex = vertexWith(graph, "nice black", w[0]);
    final Neo4jVariantGraphVertex badWhiteVertex = vertexWith(graph, "bad white", w[1]);
    final Neo4jVariantGraphVertex catVertex = vertexWith(graph, "cat", w[0]);
    final Neo4jVariantGraphVertex sharedVertex = vertexWith(graph, "shared", w[0]);
    final Neo4jVariantGraphVertex spilledVertex = vertexWith(graph, "spilled", w[1]);
    final Neo4jVariantGraphVertex hisFoodVertex = vertexWith(graph, "his food", w[0]);
    final Neo4jVariantGraphVertex againVertex = vertexWith(graph, "again", w[1]);

    assertHasWitnesses(edgeBetween(graph.getStart(), theVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(theVertex, niceBlackVertex), w[0]);
    assertHasWitnesses(edgeBetween(niceBlackVertex, catVertex), w[0]);
    assertHasWitnesses(edgeBetween(theVertex, badWhiteVertex), w[1]);
    assertHasWitnesses(edgeBetween(badWhiteVertex, catVertex), w[1]);
    assertHasWitnesses(edgeBetween(catVertex, sharedVertex), w[0]);
    assertHasWitnesses(edgeBetween(sharedVertex, hisFoodVertex), w[0]);
    assertHasWitnesses(edgeBetween(catVertex, spilledVertex), w[1]);
    assertHasWitnesses(edgeBetween(spilledVertex, hisFoodVertex), w[1]);
    assertHasWitnesses(edgeBetween(hisFoodVertex, againVertex), w[1]);
  }

  @Test
  public void joinTwoDifferentWitnesses2() {
    final SimpleWitness[] w = createWitnesses("Blackie, the black cat", "Whitney, the white cat");
    final Neo4jVariantGraph graph = collate(w).join();

    final Neo4jVariantGraphVertex blackieVertex = vertexWith(graph, "blackie", w[0]);
    final Neo4jVariantGraphVertex whitneyVertex = vertexWith(graph, "whitney", w[1]);
    final Neo4jVariantGraphVertex theVertex = vertexWith(graph, "the", w[0]);
    final Neo4jVariantGraphVertex blackVertex = vertexWith(graph, "black", w[0]);
    final Neo4jVariantGraphVertex whiteVertex = vertexWith(graph, "white", w[1]);
    final Neo4jVariantGraphVertex catVertex = vertexWith(graph, "cat", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), blackieVertex), w[0]);
    assertHasWitnesses(edgeBetween(blackieVertex, theVertex), w[0]);
    assertHasWitnesses(edgeBetween(graph.getStart(), whitneyVertex), w[1]);
    assertHasWitnesses(edgeBetween(whitneyVertex, theVertex), w[1]);
    assertHasWitnesses(edgeBetween(theVertex, blackVertex), w[0]);
    assertHasWitnesses(edgeBetween(blackVertex, catVertex), w[0]);
    assertHasWitnesses(edgeBetween(theVertex, whiteVertex), w[1]);
    assertHasWitnesses(edgeBetween(whiteVertex, catVertex), w[1]);
  }

  @Test
  public void joinTwoDifferentWitnessesWithTranspositions() {
    final SimpleWitness[] w = createWitnesses("voor Zo nu en dan zin2 na voor", "voor zin2 Nu en dan voor");
    final Neo4jVariantGraph graph = collate(w).join();
    SimpleVariantGraphSerializer s = new SimpleVariantGraphSerializer(graph);
    StringWriter writer = new StringWriter();
    s.toDot(graph, writer);
    LOG.debug("dot={}", writer.toString());

    final Neo4jVariantGraphVertex voorVertex1 = vertexWith(graph, "voor", w[0]);
    final Neo4jVariantGraphVertex zoVertex = vertexWith(graph, "zo", w[0]);
    final Neo4jVariantGraphVertex nuendanVertex = vertexWith(graph, "nu en dan", w[0]);
    //    final VariantGraphVertex zin2AVertex = vertexWith(graph, "zin2", w[0]);
    final Neo4jVariantGraphVertex zin2BVertex = vertexWith(graph, "zin2", w[1]);
    //    final VariantGraphVertex naVertex = vertexWith(graph, "na", w[0]);
    //    final VariantGraphVertex voorVertex2 = vertexWith(graph, "voor", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), voorVertex1), w[0], w[1]);
    assertHasWitnesses(edgeBetween(voorVertex1, zoVertex), w[0]);
    assertHasWitnesses(edgeBetween(zoVertex, nuendanVertex), w[0]);
    //    assertHasWitnesses(edgeBetween(nuendanVertex, zin2AVertex), w[0]);
    //    assertHasWitnesses(edgeBetween(zin2AVertex, naVertex), w[0]);
    //    assertHasWitnesses(edgeBetween(naVertex, voorVertex2), w[0]);
    //    assertHasWitnesses(edgeBetween(voorVertex2, graph.getEnd()), w[0], w[1]);

    assertHasWitnesses(edgeBetween(voorVertex1, zin2BVertex), w[1]);
    assertHasWitnesses(edgeBetween(zin2BVertex, nuendanVertex), w[1]);
    //    assertHasWitnesses(edgeBetween(nuendanVertex, voorVertex2), w[1]);
  }
}
