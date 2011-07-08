package eu.interedition.text.repository;

import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.xml.XMLParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@RequestMapping(XMLController.URL_PREFIX)
@Transactional
public class XMLController {
  public static final String URL_PREFIX = "/xml";

  @Autowired
  private RelationalTextRepository textRepository;

  @Autowired
  private XMLParser xmlParser;

  @RequestMapping(value = "/parse/{id}", method = RequestMethod.POST)
  public String parse(@PathVariable("id") int id, @RequestBody XMLParserConfigurationBean pc) throws XMLStreamException, IOException {
    return TextController.redirectTo(xmlParser.parse(textRepository.load(id), pc));
  }
}
