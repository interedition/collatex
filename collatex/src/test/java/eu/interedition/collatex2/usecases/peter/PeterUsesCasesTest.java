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

package eu.interedition.collatex2.usecases.peter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.output.rankedgraph.IRankedVariantGraphVertex;
import eu.interedition.collatex2.implementation.output.rankedgraph.VariantGraphRanker;
import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraph;
import eu.interedition.collatex2.implementation.output.segmented_graph.NonSegmentedGraphConverter;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class PeterUsesCasesTest {
  private static CollateXEngine factory;
  
  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }
  
  @Test
  public void testRanking() {
    IWitness a = factory.createWitness("A", "The black cat");
    IWitness b = factory.createWitness("B", "The black and white cat");
    IWitness c = factory.createWitness("C", "The black and green cat");
    IVariantGraph graph = factory.graph(a, b, c);
    NonSegmentedGraphConverter converter = new NonSegmentedGraphConverter();
    ISegmentedVariantGraph segmentedVariantGraph = converter.convertGraph(graph);
    VariantGraphRanker ranker = new VariantGraphRanker(segmentedVariantGraph);
    List<IRankedVariantGraphVertex> vertices = ranker.getRankedVertices();
    assertEquals("the", vertices.get(0).getVertex().getNormalized());
    assertEquals(1, vertices.get(0).getRank());
    assertEquals("black", vertices.get(1).getVertex().getNormalized());
    assertEquals(2, vertices.get(1).getRank());
    assertEquals("and", vertices.get(2).getVertex().getNormalized());
    assertEquals(3, vertices.get(2).getRank());
    assertEquals("white", vertices.get(3).getVertex().getNormalized());
    assertEquals(4, vertices.get(3).getRank());
    assertEquals("green", vertices.get(4).getVertex().getNormalized());
    assertEquals(4, vertices.get(4).getRank());
    assertEquals("cat", vertices.get(5).getVertex().getNormalized());
    assertEquals(5, vertices.get(5).getRank());
  }

  @Test
  public void testAgastTranspositionHandeling() {
    IWitness a = factory.createWitness("A", "He was agast, so");
    IWitness b = factory.createWitness("B", "He was agast");
    IWitness c = factory.createWitness("C", "So he was agast");
    IVariantGraph graph = factory.graph(a, b, c);
    NonSegmentedGraphConverter converter = new NonSegmentedGraphConverter();
    ISegmentedVariantGraph segmentedVariantGraph = converter.convertGraph(graph);
    VariantGraphRanker ranker = new VariantGraphRanker(segmentedVariantGraph);
    List<IRankedVariantGraphVertex> vertices = ranker.getRankedVertices();
    assertEquals("so", vertices.get(0).getVertex().getNormalized());
    assertEquals(1, vertices.get(0).getRank());
    assertEquals("he", vertices.get(1).getVertex().getNormalized());
    assertEquals(2, vertices.get(1).getRank());
    assertEquals("was", vertices.get(2).getVertex().getNormalized());
    assertEquals(3, vertices.get(2).getRank());
    assertEquals("agast", vertices.get(3).getVertex().getNormalized());
    assertEquals(4, vertices.get(3).getRank());
    assertEquals("so", vertices.get(4).getVertex().getNormalized());
    assertEquals(5, vertices.get(4).getRank());
  }
}
