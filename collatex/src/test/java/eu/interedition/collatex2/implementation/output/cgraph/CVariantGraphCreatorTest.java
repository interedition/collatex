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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import eu.interedition.collatex2.AbstractTest;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class CVariantGraphCreatorTest extends AbstractTest {
  private static final Logger LOG = LoggerFactory.getLogger(CVariantGraphCreatorTest.class);

  @Test
  public void basic1() {
    final IVariantGraph cgraph = CVariantGraphCreator.getCyclicVariantGraph(merge(//
            "the nice black and white cat", "the friendly white and black cat"));

    final Set<IVariantGraphEdge> edgeSet = cgraph.edgeSet();
    assertNotNull(edgeSet);
    assertEquals(12, edgeSet.size());

    final Set<IVariantGraphVertex> cvgVertexSet = cgraph.vertexSet();
    assertNotNull(cvgVertexSet);
    assertEquals(9, cvgVertexSet.size()); // # unique tokens + start,end token
  }

  @Test
  public void basic2() {
    final IWitness[] w = createWitnesses("The black dog chases a red cat.", "A red cat chases the black dog.", "A red cat chases the yellow dog");
    final IVariantGraph cgraph = CVariantGraphCreator.getCyclicVariantGraph(merge(w));

    final Set<IVariantGraphEdge> edgeSet = cgraph.edgeSet();
    assertNotNull(edgeSet);
    assertEquals(14, edgeSet.size());

    final Set<IVariantGraphVertex> cvgVertexSet = cgraph.vertexSet();
    assertNotNull(cvgVertexSet);
    assertEquals(10, cvgVertexSet.size()); // # unique tokens + start,end token

    final Set<IWitness> edgeWitnesses = extractedEdge(cgraph, edgeSet, "red", "cat").getWitnesses();
    assertEquals(3, edgeWitnesses.size());
    assertTrue(edgeWitnesses.contains(w[0]));
    assertTrue(edgeWitnesses.contains(w[1]));
    assertTrue(edgeWitnesses.contains(w[2]));

    String dot = toDot(cgraph);
    assertNotNull(dot);
    LOG.debug(dot);
  }

  private static IVariantGraphEdge extractedEdge(IVariantGraph graph, Set<IVariantGraphEdge> edgeSet, String begin, String end) {
    for (IVariantGraphEdge cvgEdge : edgeSet) {
      String normalizedBegin = graph.getEdgeSource(cvgEdge).getNormalized();
      String normalizedEnd = graph.getEdgeTarget(cvgEdge).getNormalized();
      if (normalizedBegin.equals(begin) && normalizedEnd.equals(end)) {
        return cvgEdge;
      }
    }
    return null;
  }

  private static String toDot(IVariantGraph cgraph) {
    final Writer writer = new StringWriter();
    CDOT_EXPORTER.export(writer, cgraph);
    return writer.toString();
  }
  private static final VertexNameProvider<IVariantGraphVertex> VERTEX_ID_PROVIDER = new IntegerNameProvider<IVariantGraphVertex>();
  private static final VertexNameProvider<IVariantGraphVertex> VERTEX_LABEL_PROVIDER = new VertexNameProvider<IVariantGraphVertex>() {
    @Override
    public String getVertexName(IVariantGraphVertex v) {
      return v.getNormalized();
    }
  };

  private static final EdgeNameProvider<IVariantGraphEdge> EDGE_LABEL_PROVIDER = new EdgeNameProvider<IVariantGraphEdge>() {
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

  private static final DOTExporter<IVariantGraphVertex, IVariantGraphEdge> CDOT_EXPORTER = new DOTExporter<IVariantGraphVertex, IVariantGraphEdge>(//
      VERTEX_ID_PROVIDER, VERTEX_LABEL_PROVIDER, EDGE_LABEL_PROVIDER //
  );

}
