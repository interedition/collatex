package eu.interedition.collatex2.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.IVariantGraphEdge;
import eu.interedition.collatex2.experimental.graph.IVariantGraphVertex;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.tokenization.DefaultTokenNormalizer;
import eu.interedition.collatex2.implementation.tokenization.WhitespaceTokenizer;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenNormalizer;
import eu.interedition.collatex2.interfaces.ITokenizer;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.output.TeiParallelSegmentationApparatusBuilder;
import eu.interedition.collatex2.web.io.ApiObjectMapper;
import eu.interedition.collatex2.web.io.GraphVisualisationWrapper;

@Controller
@RequestMapping("/api/**")
public class ApiController implements InitializingBean {
  protected static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";
  protected static final String TEI_NS = "http://www.tei-c.org/ns/1.0";

  private final ITokenizer defaultTokenizer = new WhitespaceTokenizer();
  private final ITokenNormalizer defaultNormalizer = new DefaultTokenNormalizer();

  @Autowired
  private ApiObjectMapper objectMapper;

  @Override
  public void afterPropertiesSet() throws Exception {
    jsonView = new MappingJacksonJsonView();
    jsonView.setObjectMapper(objectMapper);
  }

  @RequestMapping(value = "collate", headers = { "Content-Type=application/json", "Accept-Header=application/json" }, method = RequestMethod.POST)
  public ModelAndView collateToJson(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView(jsonView, "alignment", collate(input));
  }

  @RequestMapping(value = "collate", headers = { "Content-Type=application/json", "Accept-Header=application/xml" }, method = RequestMethod.POST)
  public ModelAndView collateToTei(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView(teiView, "alignment", collate(input));
  }

  @RequestMapping(value = "collate", headers = { "Content-Type=application/json" }, method = RequestMethod.POST)
  public ModelAndView collateToHtml(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView("api/alignment", "alignment", collate(input));
  }

  @RequestMapping(value = "collate2", headers = { "Content-Type=application/json" }, method = RequestMethod.POST)
  public ModelAndView collateToInfoVis(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView("api/graph", "graph", collate2(input));
  }

  @RequestMapping(value = "collate3", headers = { "Content-Type=application/json" }, method = RequestMethod.POST)
  public ModelAndView collateToInfoVis2(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView("api/graph2", "graph", collate3(input));
  }

  @RequestMapping(value = "dot", headers = { "Content-Type=application/json" }, method = RequestMethod.POST)
  public ModelAndView collateToDot(@RequestBody final ApiInput input) throws Exception {
    return new ModelAndView("api/dot", "dot", collate4(input));
  }

  @RequestMapping(value = "collate")
  public void documentation() {}

  private IAlignmentTable collate(ApiInput input) throws ApiException {
    final List<ApiWitness> witnesses = checkInputAndExtractWitnesses(input);
    return new CollateXEngine().align(witnesses.toArray(new ApiWitness[witnesses.size()]));
  }

  private IVariantGraph collate2(ApiInput input) throws ApiException {
    final List<ApiWitness> witnesses = checkInputAndExtractWitnesses(input);
    ApiWitness[] array = witnesses.toArray(new ApiWitness[witnesses.size()]);
    return new CollateXEngine().graph(array);
  }

  private GraphVisualisationWrapper collate3(ApiInput input) throws ApiException {
    final List<ApiWitness> witnesses = checkInputAndExtractWitnesses(input);
    ApiWitness[] array = witnesses.toArray(new ApiWitness[witnesses.size()]);
    return new GraphVisualisationWrapper(array, new CollateXEngine().graph(array));
  }

  private String collate4(ApiInput input) throws ApiException {
    final List<ApiWitness> witnesses = checkInputAndExtractWitnesses(input);
    ApiWitness[] array = witnesses.toArray(new ApiWitness[witnesses.size()]);
    IVariantGraph graph = new CollateXEngine().graph(array);
    VertexNameProvider<IVariantGraphVertex> vertexIDProvider = new IntegerNameProvider<IVariantGraphVertex>();
    VertexNameProvider<IVariantGraphVertex> vertexLabelProvider = new VertexNameProvider<IVariantGraphVertex>() {
      @Override
      public String getVertexName(IVariantGraphVertex v) {
        //        List<String> witnessLabels = Lists.newArrayList();
        //        for (IWitness witness : v.getWitnesses()) {
        //          witnessLabels.add(witness.getSigil() + ":" + v.getToken(witness).getContent());
        //        }
        //        Collections.sort(witnessLabels);
        //        return Joiner.on(",").join(witnessLabels);
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
        Iterable<INormalizedToken> tokens = Iterables.transform(defaultTokenizer.tokenize(sigil, witness.getContent()), defaultNormalizer);
        witness.setTokens(Lists.newArrayList(Iterables.transform(tokens, TO_API_TOKEN)));
      }

      int tokenPosition = 0;
      for (ApiToken token : witness.getApiTokens()) {
        if (token.getContent() == null || token.getContent().trim().length() == 0) {
          throw new ApiException("Empty token in " + sigil);
        }
        token.setSigil(sigil);
        token.setPosition(++tokenPosition);
        if (token.getNormalized() == null || token.getNormalized().trim().length() == 0) {
          token.setNormalized(defaultNormalizer.apply(token).getNormalized());
        }
      }
    }
    final List<ApiWitness> witnesses = input.getWitnesses();
    return witnesses;
  }

  @ExceptionHandler( { ApiException.class, JsonParseException.class })
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
      IAlignmentTable alignmentTable = (IAlignmentTable) model.get("alignment");
      Assert.notNull(alignmentTable);

      Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = xml.createElementNS(COLLATEX_NS, "collatex:apparatus");
      xml.appendChild(root);
      root.setAttribute("xmlns", TEI_NS);

      TeiParallelSegmentationApparatusBuilder.build(new CollateXEngine().createApparatus(alignmentTable), root);

      response.setContentType("application/xml");
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      TransformerUtils.enableIndenting(transformer, 4);
      transformer.transform(new DOMSource(xml), new StreamResult(out));
      out.flush();
    }
  };
}
