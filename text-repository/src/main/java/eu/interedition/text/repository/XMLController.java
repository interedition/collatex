package eu.interedition.text.repository;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.QName;
import eu.interedition.text.Text;
import eu.interedition.text.query.Operator;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.repository.model.XMLParserConfigurationImpl;
import eu.interedition.text.repository.model.XMLSerializerConfigurationImpl;
import eu.interedition.text.xml.XMLParser;
import eu.interedition.text.xml.XMLParserModule;
import eu.interedition.text.xml.XMLSerializer;
import eu.interedition.text.xml.module.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static eu.interedition.text.query.Criteria.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@RequestMapping(XMLController.URL_PREFIX)
@Transactional
public class XMLController {
  public static final String URL_PREFIX = "/xml";

  @Autowired
  private AnnotationRepository annotationRepository;

  @Autowired
  private RelationalTextRepository textRepository;

  @Autowired
  private XMLParser xmlParser;

  @Autowired
  private XMLSerializer xmlSerializer;

  private SAXTransformerFactory transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();


  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public void xml(@PathVariable("id") long id, HttpServletResponse response) throws Exception {
    xml(id, new XMLSerializerConfigurationImpl(), response);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.POST)
  public void xml(@PathVariable("id") long id, @RequestBody XMLSerializerConfigurationImpl sc, HttpServletResponse response) throws Exception {
    final List<QName> hierarchy = sc.getHierarchy();
    if (sc.isHierarchyOnly() && !hierarchy.isEmpty()) {
      final Operator hierarchyDisjunction = or();
      for (QName name : hierarchy) {
        hierarchyDisjunction.add(annotationName(name));
      }
      sc.setQuery(hierarchyDisjunction);
    }

    response.setCharacterEncoding(Text.CHARSET.name());
    response.setContentType(MediaType.APPLICATION_XML.toString());

    final TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
    transformerHandler.setResult(new StreamResult(response.getWriter()));
    xmlSerializer.serialize(transformerHandler, textRepository.load(id), sc);
  }

  @RequestMapping(value = "/{id}/parse", method = RequestMethod.GET)
  public ModelAndView form(@PathVariable("id") long id) {
    final Text text = textRepository.load(id);
    Preconditions.checkArgument(text.getType() == Text.Type.XML);

    Map<String, List<String>> names = Maps.newHashMap();
    for (QName name : annotationRepository.names(text)) {
      final URI namespaceURI = name.getNamespaceURI();
      final String ns = (namespaceURI == null ? "" : namespaceURI.toString());
      List<String> localNames = names.get(ns);
      if (localNames == null) {
        names.put(ns, localNames = Lists.newArrayList());
      }
      localNames.add(name.getLocalName());
    }
    return new ModelAndView("xml_parse").addObject("text", text).addObject("names", names);
  }

  @RequestMapping(value = "/{id}/parse", method = RequestMethod.POST)
  public String parse(@PathVariable("id") long id, @RequestBody XMLParserConfigurationImpl pc) throws XMLStreamException, IOException {
    final List<XMLParserModule> modules = pc.getModules();
    modules.add(new LineElementXMLParserModule());
    modules.add(new NotableCharacterXMLParserModule());
    modules.add(new TextXMLParserModule(textRepository));
    modules.add(new DefaultAnnotationXMLParserModule(annotationRepository, 1000));
    modules.add(new CLIXAnnotationXMLParserModule(annotationRepository, 1000));
    if (pc.isTransformTEI()) {
      modules.add(new TEIAwareAnnotationXMLParserModule(annotationRepository, 1000));
    }

    final Text parsed = xmlParser.parse(textRepository.load(id), pc);
    if (pc.isRemoveEmpty()) {
      annotationRepository.delete(and(text(parsed), rangeLength(0)));
    }
    return TextController.redirectTo(parsed);
  }
}
