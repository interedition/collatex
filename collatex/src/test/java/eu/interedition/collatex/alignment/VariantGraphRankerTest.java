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

package eu.interedition.collatex.alignment;

import com.google.common.collect.Lists;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class VariantGraphRankerTest extends AbstractTest {

  @Test
  public void ranking() {
    final VariantGraph graph = merge("The black cat", "The black and white cat", "The black and green cat").rank();
    final List<VariantGraphVertex> vertices = Lists.newArrayList(graph.vertices());

    assertVertexEquals("the", vertices.get(1));
    assertEquals(1, vertices.get(1).getRank());
    assertVertexEquals("black", vertices.get(2));
    assertEquals(2, vertices.get(2).getRank());
    assertVertexEquals("and", vertices.get(3));
    assertEquals(3, vertices.get(3).getRank());
    assertVertexEquals("white", vertices.get(4));
    assertEquals(4, vertices.get(4).getRank());
    assertVertexEquals("green", vertices.get(5));
    assertEquals(4, vertices.get(5).getRank());
    assertVertexEquals("cat", vertices.get(6));
    assertEquals(5, vertices.get(6).getRank());
  }

  @Test
  public void agastTranspositionHandling() {
    final VariantGraph graph = merge("He was agast, so", "He was agast", "So he was agast").rank();
    final List<VariantGraphVertex> vertices = Lists.newArrayList(graph.vertices());

    assertVertexEquals("so", vertices.get(1));
    assertEquals(1, vertices.get(1).getRank());
    assertVertexEquals("he", vertices.get(2));
    assertEquals(2, vertices.get(2).getRank());
    assertVertexEquals("was", vertices.get(3));
    assertEquals(3, vertices.get(3).getRank());
    assertVertexEquals("agast", vertices.get(4));
    assertEquals(4, vertices.get(4).getRank());
    assertVertexEquals("so", vertices.get(5));
    assertEquals(5, vertices.get(5).getRank());
  }
}
