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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import eu.interedition.collatex2.implementation.vg_alignment.VariantGraphBuilder;
import junit.framework.Assert;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  @Test
  public void testEmptyGraph() {
    IVariantGraph graph = new VariantGraph();
    assertEquals(2, graph.vertexSet().size());
    IVariantGraphVertex startVertex = graph.getStartVertex();
    assertEquals("#", startVertex.getNormalized());
    IVariantGraphVertex endVertex = graph.getEndVertex();
    assertEquals("#", endVertex.getNormalized());
    assertEquals(0, graph.edgeSet().size());
    assertEquals(0, graph.getWitnesses().size());
    assertTrue(graph.isEmpty());
  }

  @Test
  public void testOneWitness() {
    IWitness a = engine.createWitness("A", "only one witness");
    IVariantGraph graph = engine.graph(a);
    final Set<IVariantGraphVertex> vertices = graph.vertexSet();
    assertEquals(5, vertices.size());
    Iterator<IVariantGraphVertex> vertexI = graph.iterator();
    final IVariantGraphVertex startVertex = vertexI.next();
    final IVariantGraphVertex firstVertex = vertexI.next();
    final IVariantGraphVertex secondVertex = vertexI.next();
    final IVariantGraphVertex thirdVertex = vertexI.next();
    final IVariantGraphVertex endVertex = vertexI.next();
    assertEquals("#", startVertex.getNormalized());
    assertEquals("only", firstVertex.getNormalized());
    assertEquals("one", secondVertex.getNormalized());
    assertEquals("witness", thirdVertex.getNormalized());
    assertEquals("#", endVertex.getNormalized());
    Set<IVariantGraphEdge> edges = graph.edgeSet();
    assertEquals(4, edges.size());
    Iterator<IVariantGraphEdge> edgeI = edges.iterator();
    assertTrue(edgeI.next().getWitnesses().contains(a));
    assertTrue(edgeI.next().getWitnesses().contains(a));
    assertTrue(edgeI.next().getWitnesses().contains(a));
    assertTrue(edgeI.next().getWitnesses().contains(a));
    assertTrue(graph.containsEdge(startVertex, firstVertex));
    assertTrue(graph.containsEdge(firstVertex, secondVertex));
    assertTrue(graph.containsEdge(secondVertex, thirdVertex));
    assertTrue(graph.containsEdge(thirdVertex, endVertex));
  }

  @Test
  public void testFirstWitness() {
    IWitness a = engine.createWitness("A", "the first witness");
    IVariantGraph graph = engine.graph(a);
    // vertices
    Iterator<IVariantGraphVertex> iterator = graph.iterator();
    IVariantGraphVertex start = iterator.next();
    IVariantGraphVertex the = iterator.next();
    IVariantGraphVertex first = iterator.next();
    IVariantGraphVertex witness = iterator.next();
    IVariantGraphVertex end = iterator.next();
    Assert.assertFalse(iterator.hasNext());
    Assert.assertEquals("#", start.getNormalized());
    Assert.assertEquals("the", the.getNormalized());
    Assert.assertEquals("first", first.getNormalized());
    Assert.assertEquals("witness", witness.getNormalized());
    Assert.assertEquals("#", end.getNormalized());
    // tokens on vertices
    Assert.assertEquals("the", the.getToken(a).getContent());
    Assert.assertEquals("first", first.getToken(a).getContent());
    Assert.assertEquals("witness", witness.getToken(a).getContent());
    // edges
    Assert.assertTrue(graph.containsEdge(start, the));
    Assert.assertTrue(graph.containsEdge(the, first));
    Assert.assertTrue(graph.containsEdge(first, witness));
    Assert.assertTrue(graph.containsEdge(witness, end));
    // witnesses on edges
    Set<IVariantGraphEdge> edgeSet = graph.edgeSet();
    for (IVariantGraphEdge edge : edgeSet) {
      Assert.assertTrue("Witness " + a.getSigil() + " not present in set!", edge.containsWitness(a));
    }
  }

  @Test
  public void testLongestPath() {
    IWitness w1 = engine.createWitness("A", "a");
    IWitness w2 = engine.createWitness("B", "b");
    IWitness w3 = engine.createWitness("C", "a b");
    VariantGraph graph = new VariantGraph();
    VariantGraphBuilder builder = engine.createVariantGraphBuilder(graph);
    builder.add(w1, w2, w3);
    assertEquals(4, graph.vertexSet().size());
    VariantGraphUtil util = new VariantGraphUtil(graph);
    List<IVariantGraphVertex> longestPath = util.getLongestPath();
    //    for (CollateXVertex v: longestPath) {
    //      System.out.println(v.getNormalized());
    //    }
    assertEquals("a", longestPath.get(0).getNormalized());
    assertEquals("b", longestPath.get(1).getNormalized());
    assertEquals(2, longestPath.size());
  }

  @Test
  public void testGetPathForWitness() {
    final IWitness w1 = engine.createWitness("V", "a b c d e f ");
    final IWitness w2 = engine.createWitness("W", "x y z d e");
    final IWitness w3 = engine.createWitness("X", "a b x y z");
    IVariantGraph graph = engine.graph(w1, w2, w3);
    List<IVariantGraphEdge> path = graph.getPath(w1);
    assertEquals("#", graph.getEdgeSource(path.get(0)).getNormalized());
    assertEquals("a", graph.getEdgeTarget(path.get(0)).getNormalized());
    assertEquals("b", graph.getEdgeTarget(path.get(1)).getNormalized());
    assertEquals("c", graph.getEdgeTarget(path.get(2)).getNormalized());
    assertEquals("d", graph.getEdgeTarget(path.get(3)).getNormalized());
    assertEquals("e", graph.getEdgeTarget(path.get(4)).getNormalized());
    assertEquals("f", graph.getEdgeTarget(path.get(5)).getNormalized());
    assertEquals("#", graph.getEdgeTarget(path.get(6)).getNormalized());
    assertEquals(7, path.size());
  }
  
  @Test
  public void testTranspositions() {
	  final IWitness w1 = engine.createWitness("A", "the black and white cat");
	  final IWitness w2 = engine.createWitness("B", "the white and black cat");
	  IVariantGraph graph = engine.graph(w1, w2);
	  Map<IVariantGraphVertex, IVariantGraphVertex> transposed = graph.getTransposedTokens();
	  assertEquals(2, transposed.size());
	  final IWitness w3 = engine.createWitness("C", "the black and black cat");
	  graph = engine.graph(w1, w2, w3);
	  transposed = graph.getTransposedTokens();
	  assertEquals(2, transposed.size());
	  for ( Entry<IVariantGraphVertex, IVariantGraphVertex> nodePair : transposed.entrySet()) {
		  assertEquals(nodePair.getKey().getNormalized(), nodePair.getValue().getNormalized());
	  }
  }

  @Ignore
  @Test
  public void testGraph2Dot() {
    final IWitness w1 = engine.createWitness("A", "the quick brown fox");
    final IWitness w2 = engine.createWitness("B", "the quick and the dead");
    final IWitness w3 = engine.createWitness("C", "the slow blue dead fox");
    IVariantGraph graph = engine.graph(w1, w2, w3);
    VertexNameProvider<IVariantGraphVertex> vertexIDProvider = new VertexNameProvider<IVariantGraphVertex>() {
      @Override
      public String getVertexName(IVariantGraphVertex v) {
        return v.toString().replaceAll("eu.interedition.collatex2.experimental.graph.", "").replace('@', '_');
      }
    };
    VertexNameProvider<IVariantGraphVertex> vertexLabelProvider = new VertexNameProvider<IVariantGraphVertex>() {
      @Override
      public String getVertexName(IVariantGraphVertex v) {
        //        List<String> witnessLabels = Lists.newArrayList();
        //        for (IWitness witness : v.getWitnesses()) {
        //          witnessLabels.add(witness.getSigil() + ":" + v.getToken(witness).getContent());
        //        }
        //        Collections.sort(witnessLabels);
        //        return Joiner.on(",").join(witnessLabels);
        return v.getNormalized();
      }
    };
    EdgeNameProvider<IVariantGraphEdge> edgeLabelProvider = new EdgeNameProvider<IVariantGraphEdge>() {
      @Override
      public String getEdgeName(IVariantGraphEdge e) {
        List<String> sigils = Lists.newArrayList();
        for (IWitness witness : e.getWitnesses()) {
          sigils.add(witness.getSigil());
        }
        Collections.sort(sigils);
        return Joiner.on(",").join(sigils);
      }
    };
    DOTExporter<IVariantGraphVertex, IVariantGraphEdge> exporter = new DOTExporter<IVariantGraphVertex, IVariantGraphEdge>(vertexIDProvider, vertexLabelProvider, edgeLabelProvider);
    Writer writer = new StringWriter();
    exporter.export(writer, graph);
    assertEquals("", writer.toString());
  }
}
