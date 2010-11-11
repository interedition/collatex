package eu.interedition.collatex2.implementation.output.cgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class CVariantGraphCreatorTest {
  private static final Logger LOG = LoggerFactory.getLogger(CVariantGraphCreatorTest.class);

  @Test
  public void test1() {
    CollateXEngine engine = new CollateXEngine();
    final IWitness a = engine.createWitness("A", "the nice black and white cat");
    final IWitness b = engine.createWitness("B", "the friendly white and black cat");
    ArrayList<IWitness> witnesses = Lists.newArrayList(a, b);
    IWitness[] array = witnesses.toArray(new IWitness[witnesses.size()]);
    IVariantGraph graph = engine.graph(array);
    IVariantGraph cgraph = CVariantGraphCreator.getCyclicVariantGraph(graph);

    //    Set<IVariantGraphVertex> avgVertexSet = graph.vertexSet();
    //    for (IVariantGraphVertex variantGraphVertex : avgVertexSet) {
    //      INormalizedToken vertexKey = variantGraphVertex.getVertexKey();
    //      if (vertexKey != null) {
    //        LOG.info("key for {} = {}", variantGraphVertex.getNormalized(), vertexKey);
    //      }
    //    }

    Set<IVariantGraphEdge> edgeSet = cgraph.edgeSet();
    assertNotNull(edgeSet);
    assertEquals(12, edgeSet.size());

    Set<IVariantGraphVertex> cvgVertexSet = cgraph.vertexSet();
    //    for (IVariantGraphVertex variantGraphVertex : cvgVertexSet) {
    //      LOG.info("cvgVertex={}", variantGraphVertex.getNormalized());
    //    }
    assertNotNull(cvgVertexSet);
    assertEquals(9, cvgVertexSet.size()); // # unique tokens + start,end token
  }

  @Ignore
  @Test
  public void test2() {
    CollateXEngine engine = new CollateXEngine();
    final IWitness a = engine.createWitness("A", "The black dog chases a red cat.");
    final IWitness b = engine.createWitness("B", "A red cat chases the black dog.");
    final IWitness c = engine.createWitness("C", "A red cat chases the yellow dog");
    ArrayList<IWitness> witnesses = Lists.newArrayList(a, b, c);
    IWitness[] array = witnesses.toArray(new IWitness[witnesses.size()]);
    IVariantGraph graph = engine.graph(array);
    IVariantGraph cgraph = CVariantGraphCreator.getCyclicVariantGraph(graph);

    //    Set<IVariantGraphVertex> avgVertexSet = graph.vertexSet();
    //    for (IVariantGraphVertex variantGraphVertex : avgVertexSet) {
    //      INormalizedToken vertexKey = variantGraphVertex.getVertexKey();
    //      if (vertexKey != null) {
    //        LOG.info("key for {} = {}", variantGraphVertex.getNormalized(), vertexKey);
    //      }
    //    }

    Set<IVariantGraphEdge> edgeSet = cgraph.edgeSet();
    assertNotNull(edgeSet);
    assertEquals(14, edgeSet.size());

    Set<IVariantGraphVertex> cvgVertexSet = cgraph.vertexSet();
    //    for (IVariantGraphVertex variantGraphVertex : cvgVertexSet) {
    //      LOG.info("cvgVertex={}", variantGraphVertex.getNormalized());
    //    }
    assertNotNull(cvgVertexSet);
    assertEquals(10, cvgVertexSet.size()); // # unique tokens + start,end token

    Set<IWitness> edgeWitnesses = extractedEdge(edgeSet, "red", "cat").getWitnesses();
    assertTrue(edgeWitnesses.contains(a));
    assertTrue(edgeWitnesses.contains(b));
    assertTrue(edgeWitnesses.contains(c));
    assertEquals(3, edgeWitnesses.size());
  }

  private IVariantGraphEdge extractedEdge(Set<IVariantGraphEdge> edgeSet, String begin, String end) {
    for (IVariantGraphEdge cvgEdge : edgeSet) {
      String normalizedBegin = cvgEdge.getBeginVertex().getNormalized();
      String normalizedEnd = cvgEdge.getEndVertex().getNormalized();
      if (normalizedBegin.equals(begin) && normalizedEnd.equals(end)) {
        return cvgEdge;
      }
    }
    return null;
  }
}
