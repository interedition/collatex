package eu.interedition.text.repository;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.QName;
import eu.interedition.text.Text;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.repository.io.XMLParserConfigurationBean;
import eu.interedition.text.xml.XMLParser;
import eu.interedition.text.xml.XMLParserModule;
import eu.interedition.text.xml.module.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static eu.interedition.text.query.Criteria.and;
import static eu.interedition.text.query.Criteria.rangeLength;
import static eu.interedition.text.query.Criteria.text;

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
  public String parse(@PathVariable("id") long id, @RequestBody XMLParserConfigurationBean pc) throws XMLStreamException, IOException {
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
