package eu.interedition.collatex2.implementation.output.cgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
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

    Set<IWitness> edgeWitnesses = extractedEdge(cgraph, edgeSet, "red", "cat").getWitnesses();
    assertTrue(edgeWitnesses.contains(a));
    assertTrue(edgeWitnesses.contains(b));
    assertTrue(edgeWitnesses.contains(c));
    assertEquals(3, edgeWitnesses.size());

    String dot = dotOutput(cgraph);
    assertNotNull(dot);
    LOG.info(dot);
  }

  static final VertexNameProvider<IVariantGraphVertex> VERTEX_ID_PROVIDER = new IntegerNameProvider<IVariantGraphVertex>();
  static final VertexNameProvider<IVariantGraphVertex> VERTEX_LABEL_PROVIDER = new VertexNameProvider<IVariantGraphVertex>() {
    @Override
    public String getVertexName(IVariantGraphVertex v) {
      return v.getNormalized();
    }
  };
  static final EdgeNameProvider<IVariantGraphEdge> EDGE_LABEL_PROVIDER = new EdgeNameProvider<IVariantGraphEdge>() {
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
  static final DOTExporter<IVariantGraphVertex, IVariantGraphEdge> CDOT_EXPORTER = new DOTExporter<IVariantGraphVertex, IVariantGraphEdge>(//
      VERTEX_ID_PROVIDER, VERTEX_LABEL_PROVIDER, EDGE_LABEL_PROVIDER //
  );

  private String dotOutput(IVariantGraph cgraph) {
    Writer writer = new StringWriter();
    CDOT_EXPORTER.export(writer, cgraph);
    String string = writer.toString();
    return string;
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
