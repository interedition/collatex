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

package eu.interedition.collatex2.web;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.input.tokenization.DefaultTokenNormalizer;
import eu.interedition.collatex2.implementation.input.tokenization.WhitespaceTokenizer;
import eu.interedition.collatex2.implementation.output.apparatus.TeiParallelSegmentationApparatusBuilder;
import eu.interedition.collatex2.implementation.output.cgraph.CVariantGraphCreator;
import eu.interedition.collatex2.implementation.output.graphml.GraphMLBuilder;
import eu.interedition.collatex2.implementation.output.jgraph.JVariantGraphCreator;
import eu.interedition.collatex2.interfaces.*;
import eu.interedition.collatex2.interfaces.nonpublic.joined_graph.IJVariantGraph;
import eu.interedition.collatex2.interfaces.nonpublic.joined_graph.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.nonpublic.joined_graph.IJVariantGraphVertex;
import eu.interedition.collatex2.web.io.ApiObjectMapper;
import org.codehaus.jackson.JsonParseException;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.xml.TransformerUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

@Controller
public class ApiController implements InitializingBean {
  protected static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";
  protected static final String TEI_NS = "http://www.tei-c.org/ns/1.0";
  private static final String GRAPHVIZ_DOT = System.getProperty("collatex.graphviz.dot", "/usr/bin/dot");

  private final ITokenizer defaultTokenizer = new WhitespaceTokenizer();
  private final ITokenNormalizer defaultNormalizer = new DefaultTokenNormalizer();


  @Autowired
  private ApiObjectMapper objectMapper;

  @Override
  public void afterPropertiesSet() throws Exception {
    jsonView = new MappingJacksonJsonView();
    jsonView.setObjectMapper(objectMapper);
  }

  @RequestMapping(value = "/")
  public String redirectFromRoot() {
    return "redirect:/api/collate";
  }

  @RequestMapping(value = "/api/collate", headers = {"Content-Type=application/json", "Accept=application/json"}, method = RequestMethod.POST)
  public ModelAndView collateToJson(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView(jsonView, "alignment", collate(input));
  }

  @RequestMapping(value = "/api/collate", headers = {"Content-Type=application/json", "Accept=application/xml"}, method = RequestMethod.POST)
  public ModelAndView collateToTei(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView(teiView, "alignment", collateToGraph(input));
  }

  @RequestMapping(value = "/api/collate", headers = {"Content-Type=application/json", "Accept=image/svg+xml"}, method = RequestMethod.POST)
  public void collateToSvg(@RequestBody final ApiInput input, HttpServletResponse response) throws Exception {
    // final String dot = ccollate2dot(input); // cyclic, unjoined graph
    // final String dot = collate2dot(input); // acyclic unjoined graph
    final String dot = jcollate2dot(input);

    final Process dotProc = Runtime.getRuntime().exec(GRAPHVIZ_DOT + " -Grankdir=LR -Gid=VariantGraph -Tsvg");
    final OutputStream dotStdin = new BufferedOutputStream(dotProc.getOutputStream());
    try {
      ByteStreams.copy(ByteStreams.newInputStreamSupplier(dot.getBytes(Charset.defaultCharset())), dotStdin);
    } finally {
      Closeables.close(dotStdin, false);
    }

    InputStream svgResult = null;
    final FileBackedOutputStream svgBuf = new FileBackedOutputStream(102400);
    try {
      ByteStreams.copy(svgResult = new BufferedInputStream(dotProc.getInputStream()), svgBuf);
    } finally {
      Closeables.close(svgBuf, false);
      Closeables.close(svgResult, false);
    }

    InputStream svgSource = null;
    try {
      if (dotProc.waitFor() == 0) {
        response.setContentType("image/svg+xml");

        final OutputStream responseStream = response.getOutputStream();
        ByteStreams.copy(svgSource = svgBuf.getSupplier().getInput(), responseStream);
        responseStream.flush();

        return;
      }
    } catch (InterruptedException e) {
    } finally {
      Closeables.closeQuietly(svgSource);
    }

    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  @RequestMapping(value = "/api/collate", headers = {"Content-Type=application/json", "Accept=application/graphml+xml"}, method = RequestMethod.POST)
  public ModelAndView collateToGraphML(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView(graphMLView, "alignment", collateToGraph(input));
  }


  @RequestMapping(value = "/api/collate", headers = {"Content-Type=application/json", "Accept=application/xhtml+xml;charset=utf-8"}, method = RequestMethod.POST)
  public ModelAndView collateToHtml(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView("api/alignment", "alignment", collate(input));
  }

  //  @RequestMapping(value = "collate", headers = { "Content-Type=application/json" }, method = RequestMethod.POST)
  //  public ModelAndView collateToHtmlP(@RequestBody final ApiInput input) throws Exception {
  //    List<Map<String, Object>> rows = parallelSegmentationRows(input);
  //    return new ModelAndView("api/apparatus", "rows", rows);
  //  }

  static final Phrase EMPTY_PHRASE = new Phrase(Lists.<INormalizedToken>newArrayList());

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

  private Map<String, Object> rowMap(String sigil, Collection<String> phrases) {
    Map<String, Object> row = Maps.newHashMap();
    row.put("sigil", sigil);
    row.put("cells", phrases);
    return row;
  }

  @RequestMapping(value = "/api/collate")
  public void documentation() {
  }

  private IAlignmentTable collate(ApiInput input) throws ApiException {
    final List<ApiWitness> witnesses = checkInputAndExtractWitnesses(input);
    return new CollateXEngine().align(witnesses.toArray(new ApiWitness[witnesses.size()]));
  }

  private IVariantGraph collateToGraph(ApiInput input) throws ApiException {
    final List<ApiWitness> witnesses = checkInputAndExtractWitnesses(input);
    return new CollateXEngine().graph(witnesses.toArray(new ApiWitness[witnesses.size()]));
  }

  private String collate2dot(ApiInput input) throws ApiException {
    final List<ApiWitness> witnesses = checkInputAndExtractWitnesses(input);
    ApiWitness[] array = witnesses.toArray(new ApiWitness[witnesses.size()]);
    IVariantGraph graph = new CollateXEngine().graph(array);
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

  static final VertexNameProvider<IJVariantGraphVertex> JVERTEX_ID_PROVIDER = new IntegerNameProvider<IJVariantGraphVertex>();
  static final VertexNameProvider<IJVariantGraphVertex> JVERTEX_LABEL_PROVIDER = new VertexNameProvider<IJVariantGraphVertex>() {
    @Override
    public String getVertexName(IJVariantGraphVertex v) {
      return v.getNormalized();
    }
  };
  static final EdgeNameProvider<IJVariantGraphEdge> JEDGE_LABEL_PROVIDER = new EdgeNameProvider<IJVariantGraphEdge>() {
    @Override
    public String getEdgeName(IJVariantGraphEdge e) {
      List<String> sigils = Lists.newArrayList();
      for (IWitness witness : e.getWitnesses()) {
        sigils.add(witness.getSigil());
      }
      Collections.sort(sigils);
      return Joiner.on(",").join(sigils);
    }
  };
  static final DOTExporter<IJVariantGraphVertex, IJVariantGraphEdge> JDOT_EXPORTER = new DOTExporter<IJVariantGraphVertex, IJVariantGraphEdge>(//
          JVERTEX_ID_PROVIDER, JVERTEX_LABEL_PROVIDER, JEDGE_LABEL_PROVIDER //
  );
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

  private String jcollate2dot(ApiInput input) throws ApiException {
    final List<ApiWitness> witnesses = checkInputAndExtractWitnesses(input);
    ApiWitness[] array = witnesses.toArray(new ApiWitness[witnesses.size()]);
    IVariantGraph graph = new CollateXEngine().graph(array);

    JVariantGraphCreator creator = new JVariantGraphCreator();
    IJVariantGraph jgraph = creator.parallelSegmentate(graph);
    Writer writer = new StringWriter();
    JDOT_EXPORTER.export(writer, jgraph);
    return writer.toString();
  }

  private String ccollate2dot(ApiInput input) throws ApiException {
    final List<ApiWitness> witnesses = checkInputAndExtractWitnesses(input);
    ApiWitness[] array = witnesses.toArray(new ApiWitness[witnesses.size()]);
    IVariantGraph graph = new CollateXEngine().graph(array);

    IVariantGraph cgraph = CVariantGraphCreator.getCyclicVariantGraph(graph);
    Writer writer = new StringWriter();
    CDOT_EXPORTER.export(writer, cgraph);
    return writer.toString();
  }

  private List<ApiWitness> checkInputAndExtractWitnesses(ApiInput input) throws ApiException {
    Set<String> sigle = new HashSet<String>();
    for (ApiWitness witness : input.getWitnesses()) {
      String sigil = witness.getSigil();
      if (sigil == null) {
        throw new ApiException("Witness without id/sigil given");
      }
      if (sigle.contains(sigil)) {
        throw new ApiException("Duplicate id/sigil: " + sigil);
      }
      sigle.add(sigil);

      if ((witness.getTokens() == null) && (witness.getContent() != null)) {
        Iterable<INormalizedToken> tokens = Iterables.transform(defaultTokenizer.tokenize(witness.getContent()), defaultNormalizer);
        witness.setTokens(Lists.newArrayList(Iterables.transform(tokens, TO_API_TOKEN)));
      }

      int tokenPosition = 0;
      for (ApiToken token : witness.getApiTokens()) {
        token.setPosition(++tokenPosition);
        if (token.getNormalized() == null || token.getNormalized().trim().length() == 0) {
          token.setNormalized(defaultNormalizer.apply(token).getNormalized());
        }
      }
    }
    final List<ApiWitness> witnesses = input.getWitnesses();
    return witnesses;
  }

  @ExceptionHandler({ApiException.class, JsonParseException.class})
  public ModelAndView apiError(HttpServletResponse response, Exception exception) {
    return new ModelAndView(new MappingJacksonJsonView(), new ModelMap("error", exception.getMessage()));
  }

  private static final Function<INormalizedToken, ? extends INormalizedToken> TO_API_TOKEN = new Function<INormalizedToken, ApiToken>() {

    @Override
    public ApiToken apply(INormalizedToken from) {
      return new ApiToken(from);
    }
  };

  private MappingJacksonJsonView jsonView;

  private final AbstractView teiView = new AbstractView() {

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
      IVariantGraph variantGraph = (IVariantGraph) model.get("alignment");
      Assert.notNull(variantGraph);

      Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = xml.createElementNS(COLLATEX_NS, "collatex:apparatus");
      xml.appendChild(root);
      root.setAttribute("xmlns", TEI_NS);

      TeiParallelSegmentationApparatusBuilder.build(new CollateXEngine().createApparatus(variantGraph), root);

      response.setContentType("application/xml");
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      TransformerUtils.enableIndenting(transformer, 4);
      transformer.transform(new DOMSource(xml), new StreamResult(out));
      out.flush();
    }
  };
  private final AbstractView graphMLView = new AbstractView() {

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
      IVariantGraph variantGraph = (IVariantGraph) model.get("alignment");
      Assert.notNull(variantGraph);

      Document graphXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

      GraphMLBuilder.build(variantGraph, graphXML);

      response.setContentType("application/graphml+xml");
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      TransformerUtils.enableIndenting(transformer, 4);
      transformer.transform(new DOMSource(graphXML), new StreamResult(out));
      out.flush();
    }
  };

}
