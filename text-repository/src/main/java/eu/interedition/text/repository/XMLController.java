package eu.interedition.text.repository;

import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.xml.XMLParser;
import eu.interedition.text.xml.XMLParserModule;
import eu.interedition.text.xml.module.AnnotationStorageXMLParserModule;
import eu.interedition.text.xml.module.TextXMLParserModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

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

  @RequestMapping(value = "/parse/{id}", method = RequestMethod.POST)
  public String parse(@PathVariable("id") int id, @RequestBody XMLParserConfigurationBean pc) throws XMLStreamException, IOException {
    final List<XMLParserModule> modules = pc.getModules();
    modules.add(new TextXMLParserModule(textRepository));
    modules.add(new AnnotationStorageXMLParserModule(annotationRepository));

    return TextController.redirectTo(xmlParser.parse(textRepository.load(id), pc));
  }
}
