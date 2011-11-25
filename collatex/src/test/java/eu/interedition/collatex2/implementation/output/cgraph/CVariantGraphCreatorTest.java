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

package eu.interedition.collatex2.implementation.output.cgraph;

import java.util.Set;

import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IVariantGraphEdge;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CVariantGraphCreatorTest {
  private static final Logger LOG = LoggerFactory.getLogger(CVariantGraphCreatorTest.class);

  @Test
  public void test1() {
    //    CollateXEngine engine = new CollateXEngine();
    //    final IWitness a = engine.createWitness("A", "the nice black and white cat");
    //    final IWitness b = engine.createWitness("B", "the friendly white and black cat");
    //    ArrayList<IWitness> witnesses = Lists.newArrayList(a, b);
    //    IWitness[] array = witnesses.toArray(new IWitness[witnesses.size()]);
    //    IVariantGraph graph = engine.graph(array);
    //    IVariantGraph cgraph = CVariantGraphCreator.getCyclicVariantGraph(graph);
    //
    //    //    Set<IVariantGraphVertex> avgVertexSet = graph.vertexSet();
    //    //    for (IVariantGraphVertex variantGraphVertex : avgVertexSet) {
    //    //      INormalizedToken vertexKey = variantGraphVertex.getVertexKey();
    //    //      if (vertexKey != null) {
    //    //        LOG.info("key for {} = {}", variantGraphVertex.getNormalized(), vertexKey);
    //    //      }
    //    //    }
    //
    //    Set<IVariantGraphEdge> edgeSet = cgraph.edgeSet();
    //    assertNotNull(edgeSet);
    //    assertEquals(12, edgeSet.size());
    //
    //    Set<IVariantGraphVertex> cvgVertexSet = cgraph.vertexSet();
    //    //    for (IVariantGraphVertex variantGraphVertex : cvgVertexSet) {
    //    //      LOG.info("cvgVertex={}", variantGraphVertex.getNormalized());
    //    //    }
    //    assertNotNull(cvgVertexSet);
    //    assertEquals(9, cvgVertexSet.size()); // # unique tokens + start,end token
  }

  @Test
  public void test2() {
    //    CollateXEngine engine = new CollateXEngine();
    //    final IWitness a = engine.createWitness("A", "The black dog chases a red cat.");
    //    final IWitness b = engine.createWitness("B", "A red cat chases the black dog.");
    //    final IWitness c = engine.createWitness("C", "A red cat chases the yellow dog");
    //    ArrayList<IWitness> witnesses = Lists.newArrayList(a, b, c);
    //    IWitness[] array = witnesses.toArray(new IWitness[witnesses.size()]);
    //    IVariantGraph graph = engine.graph(array);
    //    IVariantGraph cgraph = CVariantGraphCreator.getCyclicVariantGraph(graph);
    //
    //    //    Set<IVariantGraphVertex> avgVertexSet = graph.vertexSet();
    //    //    for (IVariantGraphVertex variantGraphVertex : avgVertexSet) {
    //    //      INormalizedToken vertexKey = variantGraphVertex.getVertexKey();
    //    //      if (vertexKey != null) {
    //    //        LOG.info("key for {} = {}", variantGraphVertex.getNormalized(), vertexKey);
    //    //      }
    //    //    }
    //
    //    Set<IVariantGraphEdge> edgeSet = cgraph.edgeSet();
    //    assertNotNull(edgeSet);
    //    assertEquals(14, edgeSet.size());
    //
    //    Set<IVariantGraphVertex> cvgVertexSet = cgraph.vertexSet();
    //    //    for (IVariantGraphVertex variantGraphVertex : cvgVertexSet) {
    //    //      LOG.info("cvgVertex={}", variantGraphVertex.getNormalized());
    //    //    }
    //    assertNotNull(cvgVertexSet);
    //    assertEquals(10, cvgVertexSet.size()); // # unique tokens + start,end token
    //
    //    Set<IWitness> edgeWitnesses = extractedEdge(cgraph, edgeSet, "red", "cat").getWitnesses();
    //    assertTrue(edgeWitnesses.contains(a));
    //    assertTrue(edgeWitnesses.contains(b));
    //    assertTrue(edgeWitnesses.contains(c));
    //    assertEquals(3, edgeWitnesses.size());
    //
    //    String dot = DotExporter.toDot(cgraph);
    //    assertNotNull(dot);
    //    LOG.info(dot);
  }

  private IVariantGraphEdge extractedEdge(IVariantGraph graph, Set<IVariantGraphEdge> edgeSet, String begin, String end) {
    for (IVariantGraphEdge cvgEdge : edgeSet) {
      String normalizedBegin = graph.getEdgeSource(cvgEdge).getNormalized();
      String normalizedEnd = graph.getEdgeTarget(cvgEdge).getNormalized();
      if (normalizedBegin.equals(begin) && normalizedEnd.equals(end)) {
        return cvgEdge;
      }
    }
    return null;
  }

}
