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

package eu.interedition.collatex.web;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex.implementation.CollateXEngine;
import eu.interedition.collatex.implementation.graph.cyclic.CyclicVariantGraph;
import eu.interedition.collatex.implementation.input.DefaultTokenNormalizer;
import eu.interedition.collatex.implementation.input.WhitespaceTokenizer;
import eu.interedition.collatex.interfaces.*;
import eu.interedition.collatex.web.model.Collation;
import eu.interedition.collatex.web.model.Token;
import eu.interedition.collatex.web.model.Witness;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

@Controller
public class CollationController {
  private final ITokenizer defaultTokenizer = new WhitespaceTokenizer();
  private final ITokenNormalizer defaultNormalizer = new DefaultTokenNormalizer();

  @RequestMapping("/")
  public String form() {
    return "collate";
  }

  @RequestMapping(value = "/align", method = RequestMethod.POST)
  @ResponseBody
  public IAlignmentTable align(@RequestBody Collation collation) throws Exception {
    return new CollateXEngine().align(checkAndExtractWitnesses(collation));
  }


  @RequestMapping(value = "/graph", method = RequestMethod.POST)
  @ResponseBody
  public IVariantGraph graph(@RequestBody Collation collation) throws Exception {
    return new CollateXEngine().graph(checkAndExtractWitnesses(collation));
  }

  //  @RequestMapping(value = "collate", headers = { "Content-Type=application/json" }, method = RequestMethod.POST)
  //  public ModelAndView collateToHtmlP(@RequestBody final ApiInput input) throws Exception {
  //    List<Map<String, Object>> rows = parallelSegmentationRows(input);
  //    return new ModelAndView("api/apparatus", "rows", rows);
  //TODO: Parallel segmentation for Alignment Tables is not enabled for the moment!
  //  private List<Map<String, Object>> parallelSegmentationRows(final ApiInput input) throws ApiException {
  //    IAlignmentTable alignmentTable = collate(input);
  //    ParallelSegmentationApparatus apparatus = new CollateXEngine().createApparatus(alignmentTable);
  //
  //    List<ApparatusEntry> entries = apparatus.getEntries();
  //    List<Map<String, Object>> rows = Lists.newArrayList();
  //    for (String sigil : alignmentTable.getSigla()) {
  //      List<String> phrases = Lists.newArrayList();
  //      for (ApparatusEntry apparatusEntry : entries) {
  //        String phrase = apparatusEntry.containsWitness(sigil) ? apparatusEntry.getPhrase(sigil).getContent() : "";
  //        phrases.add(phrase);
  //      }
  //      Map<String, Object> row = rowMap(sigil, phrases);
  //      rows.add(row);
  //    }
  //    return rows;
  //  }
  //  }

  private Map<String, Object> rowMap(String sigil, Collection<String> phrases) {
    Map<String, Object> row = Maps.newHashMap();
    row.put("sigil", sigil);
    row.put("cells", phrases);
    return row;
  }

  private Witness[] checkAndExtractWitnesses(Collation collation) throws CollationException {
    final List<Witness> witnesses = collation.getWitnesses();
    final Set<String> sigle = new HashSet<String>();

    for (Witness witness : witnesses) {
      String sigil = witness.getSigil();
      if (sigil == null) {
        throw new CollationException("Witness without id/sigil given");
      }
      if (sigle.contains(sigil)) {
        throw new CollationException("Duplicate id/sigil: " + sigil);
      }
      sigle.add(sigil);

      if ((witness.getTokens() == null) && (witness.getContent() != null)) {
        Iterable<INormalizedToken> tokens = Iterables.transform(defaultTokenizer.tokenize(witness.getContent()), defaultNormalizer);
        witness.setTokens(Lists.<INormalizedToken>newArrayList(Iterables.transform(tokens, Token.TO_TOKEN)));
      }

      for (Token token : witness.getApiTokens()) {
        if (token.getNormalized() == null || token.getNormalized().trim().length() == 0) {
          token.setNormalized(defaultNormalizer.apply(token).getNormalized());
        }
      }
    }

    return witnesses.toArray(new Witness[witnesses.size()]);
  }
  private String collate2dot(Collation collation) throws CollationException {
    IVariantGraph graph = new CollateXEngine().graph(checkAndExtractWitnesses(collation));
    VertexNameProvider<IVariantGraphVertex> vertexIDProvider = new IntegerNameProvider<IVariantGraphVertex>();
    VertexNameProvider<IVariantGraphVertex> vertexLabelProvider = new VertexNameProvider<IVariantGraphVertex>() {
      @Override
      public String getVertexName(IVariantGraphVertex v) {
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

  private String ccollate2dot(Collation collation) throws CollationException {
    final IVariantGraph graph = new CollateXEngine().graph(checkAndExtractWitnesses(collation));
    final Writer writer = new StringWriter();
    CDOT_EXPORTER.export(writer, CyclicVariantGraph.create(graph));
    return writer.toString();
  }
}
