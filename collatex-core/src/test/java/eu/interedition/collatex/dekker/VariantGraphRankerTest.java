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

package eu.interedition.collatex.dekker;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex.AbstractTest;

public class VariantGraphRankerTest extends AbstractTest {

  @Test
  public void ranking() {
    final VariantGraph graph = collate("The black cat", "The black and white cat", "The black and green cat");
    final VariantGraphRanking ranking = VariantGraphRanking.of(graph);
    final List<VariantGraph.Vertex> vertices = Lists.newArrayList(graph.vertices());

    assertVertexEquals("the", vertices.get(1));
    assertEquals(1, (long) ranking.apply(vertices.get(1)));
    assertVertexEquals("black", vertices.get(2));
    assertEquals(2, (long) ranking.apply(vertices.get(2)));
    assertVertexEquals("and", vertices.get(3));
    assertEquals(3, (long) ranking.apply(vertices.get(3)));
    assertVertexEquals("white", vertices.get(4));
    assertEquals(4, (long) ranking.apply(vertices.get(4)));
    assertVertexEquals("green", vertices.get(5));
    assertEquals(4, (long) ranking.apply(vertices.get(5)));
    assertVertexEquals("cat", vertices.get(6));
    assertEquals(5, (long) ranking.apply(vertices.get(6)));
  }

  @Test
  public void agastTranspositionHandling() {
    final VariantGraph graph = collate("He was agast, so", "He was agast", "So he was agast");
    final VariantGraphRanking ranking = VariantGraphRanking.of(graph);
    final List<VariantGraph.Vertex> vertices = Lists.newArrayList(graph.vertices());

    assertVertexEquals("so", vertices.get(1));
    assertEquals(1,(long) ranking.apply(vertices.get(1)));
    assertVertexEquals("he", vertices.get(2));
    assertEquals(2, (long) ranking.apply(vertices.get(2)));
    assertVertexEquals("was", vertices.get(3));
    assertEquals(3, (long) ranking.apply(vertices.get(3)));
    assertVertexEquals("agast", vertices.get(4));
    assertEquals(4, (long) ranking.apply(vertices.get(4)));
    assertVertexEquals("so", vertices.get(5));
    assertEquals(5, (long) ranking.apply(vertices.get(5)));
  }
}
