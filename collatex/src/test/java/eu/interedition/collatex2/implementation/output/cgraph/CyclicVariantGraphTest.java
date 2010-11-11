package eu.interedition.collatex2.implementation.output.cgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CyclicVariantGraphTest {
  @Test
  public void testCyclicVariantGraph() {
    CyclicVariantGraph cyclicVariantGraph = CyclicVariantGraph.create();
    assertTrue(cyclicVariantGraph.edgeSet().isEmpty());
    assertEquals(2, cyclicVariantGraph.vertexSet().size());
  }
}
