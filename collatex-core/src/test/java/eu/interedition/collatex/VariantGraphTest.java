/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex;

import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.util.StreamUtil;
import eu.interedition.collatex.util.VariantGraphTraversal;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class VariantGraphTest extends AbstractTest {

    @Test
    public void emptyGraph() {
        final VariantGraph graph = collate(createWitnesses());
        assertEquals(0, graph.witnesses().size());
        assetGraphSize(graph, 2, 1);
    }

    @Test
    public void getTokens() {
        final SimpleWitness[] w = createWitnesses("a b c d");
        final VariantGraph graph = collate(w);
        final List<VariantGraph.Vertex> vertices = StreamUtil.stream(VariantGraphTraversal.of(graph)).collect(Collectors.toList());
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
        final VariantGraph graph = collate(w);

        assetGraphSize(graph, 5, 4);

        final VariantGraph.Vertex firstVertex = vertexWith(graph, "only", w[0]);
        final VariantGraph.Vertex secondVertex = vertexWith(graph, "one", w[0]);
        final VariantGraph.Vertex thirdVertex = vertexWith(graph, "witness", w[0]);

        assertHasWitnesses(edgeBetween(graph.getStart(), firstVertex), w[0]);
        assertHasWitnesses(edgeBetween(firstVertex, secondVertex), w[0]);
        assertHasWitnesses(edgeBetween(secondVertex, thirdVertex), w[0]);
        assertHasWitnesses(edgeBetween(thirdVertex, graph.getEnd()), w[0]);
    }

    @Test
    public void getPathForWitness() {
        final SimpleWitness[] w = createWitnesses("a b c d e f ", "x y z d e", "a b x y z");
        final VariantGraph graph = collate(w);
        final List<VariantGraph.Vertex> path = StreamUtil.stream(VariantGraphTraversal.of(graph, Collections.singleton(w[0])))//
                .collect(Collectors.toList());

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
    public void transpositions1() {
        final VariantGraph graph = collate("the nice black and white cat", "the friendly white and black cat");
        assertGraphEdges(graph, 12);
    }

    @Test
    public void transpositions2() {
        final SimpleWitness[] w = createWitnesses("The black dog chases a red cat.", "A red cat chases the black dog.", "A red cat chases the yellow dog");
        final VariantGraph graph = collate(w);

        // There should be two vertices for cat in the graph
        assertHasWitnesses(edgeBetween(vertexWith(graph, "red", w[0]), vertexWith(graph, "cat", w[0])), w[0]);
        assertHasWitnesses(edgeBetween(vertexWith(graph, "red", w[1]), vertexWith(graph, "cat", w[1])), w[1], w[2]);

        assetGraphSize(graph, 17, 20);
    }

    @Test
    public void joinTwoIdenticalWitnesses() {
        final SimpleWitness[] w = createWitnesses("the black cat", "the black cat");
        final VariantGraph graph = VariantGraph.JOIN.apply(collate(w));

        assetGraphSize(graph, 3, 2);

        final VariantGraph.Vertex joinedVertex = vertexWith(graph, "the black cat", w[0]);

        assertHasWitnesses(edgeBetween(graph.getStart(), joinedVertex), w[0], w[1]);
        assertHasWitnesses(edgeBetween(joinedVertex, graph.getEnd()), w[0], w[1]);
    }

    @Test
    public void joinTwoDifferentWitnesses() {
        final SimpleWitness[] w = createWitnesses("the nice black cat shared his food", "the bad white cat spilled his food again");
        final VariantGraph graph = VariantGraph.JOIN.apply(collate(w));

        final VariantGraph.Vertex theVertex = vertexWith(graph, "the", w[0]);
        final VariantGraph.Vertex niceBlackVertex = vertexWith(graph, "nice black", w[0]);
        final VariantGraph.Vertex badWhiteVertex = vertexWith(graph, "bad white", w[1]);
        final VariantGraph.Vertex catVertex = vertexWith(graph, "cat", w[0]);
        final VariantGraph.Vertex sharedVertex = vertexWith(graph, "shared", w[0]);
        final VariantGraph.Vertex spilledVertex = vertexWith(graph, "spilled", w[1]);
        final VariantGraph.Vertex hisFoodVertex = vertexWith(graph, "his food", w[0]);
        final VariantGraph.Vertex againVertex = vertexWith(graph, "again", w[1]);

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
        final VariantGraph graph = VariantGraph.JOIN.apply(collate(w));

        final VariantGraph.Vertex blackieVertex = vertexWith(graph, "blackie", w[0]);
        final VariantGraph.Vertex whitneyVertex = vertexWith(graph, "whitney", w[1]);
        final VariantGraph.Vertex theVertex = vertexWith(graph, ", the", w[0]);
        final VariantGraph.Vertex blackVertex = vertexWith(graph, "black", w[0]);
        final VariantGraph.Vertex whiteVertex = vertexWith(graph, "white", w[1]);
        final VariantGraph.Vertex catVertex = vertexWith(graph, "cat", w[0]);

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
        final VariantGraph graph = VariantGraph.JOIN.apply(collate(w));
        final StringWriter writer = new StringWriter();
        new SimpleVariantGraphSerializer(graph).toDot(writer);
        LOG.log(Level.FINE, "dot={0}", writer.toString());

        final VariantGraph.Vertex voorVertex1 = vertexWith(graph, "voor", w[0]);
        final VariantGraph.Vertex zoVertex = vertexWith(graph, "zo", w[0]);
        final VariantGraph.Vertex nuendanVertex = vertexWith(graph, "nu en dan", w[0]);
        //    final VariantGraphVertex zin2AVertex = vertexWith(graph, "zin2", w[0]);
        final VariantGraph.Vertex zin2BVertex = vertexWith(graph, "zin2", w[1]);
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
