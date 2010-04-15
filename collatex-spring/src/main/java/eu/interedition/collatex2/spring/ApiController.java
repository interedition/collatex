package eu.interedition.collatex2.spring;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Controller;
import org.springframework.util.xml.TransformerUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.output.ApparatusEntry;
import eu.interedition.collatex2.output.ParallelSegmentationApparatus;

@Controller
@RequestMapping("/api/**")
public class ApiController {
  private static final String TEI_NS = "http://www.tei-c.org/ns/1.0";
  private static final String WITNESS_1 = "a b c d e f g h";
  private static final String WITNESS_2 = "d e f g x y z";

  private CollateXEngine collateXEngine = new CollateXEngine();

  @RequestMapping("collate")
  public ModelAndView collate() throws Exception {
    final IWitness witness1 = collateXEngine.createWitness("A", WITNESS_1);
    final IWitness witness2 = collateXEngine.createWitness("B", WITNESS_2);
    return new ModelAndView(new ApparatusXmlView(collateXEngine.createApparatus(collateXEngine.align(witness1, witness2))));
  }

  private static class ApparatusXmlView extends AbstractView {

    private ParallelSegmentationApparatus apparatus;
    
    private ApparatusXmlView(ParallelSegmentationApparatus apparatus) {
      this.apparatus = apparatus;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
      final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = xml.createElementNS(TEI_NS, "text");
      xml.appendChild(root);

      for (ApparatusEntry segment : apparatus.getEntries()) {
        Element app = xml.createElementNS(TEI_NS, "app");
        for (String sigle : apparatus.getSigli()) {
          Element rdg = xml.createElementNS(TEI_NS, "rdg");
          rdg.setAttribute("wit", sigle);
          app.appendChild(rdg);

          if (segment.containsWitness(sigle)) {
            rdg.setTextContent(segment.getPhrase(sigle).getContent());
          }
        }
        root.appendChild(app);
      }

      response.setContentType("application/xml");
      response.setCharacterEncoding("UTF-8");

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      TransformerUtils.enableIndenting(transformer);
      PrintWriter out = response.getWriter();
      transformer.transform(new DOMSource(xml), new StreamResult(out));
      out.flush();
    }

  }
}
