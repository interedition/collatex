package eu.interedition.text;

import eu.interedition.text.mem.SimpleQName;

import javax.xml.XMLConstants;
import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface TextConstants {
  final URI XML_NS_URI = URI.create(XMLConstants.XML_NS_URI);
  final URI TEI_NS = URI.create("http://www.tei-c.org/ns/1.0");
  final URI INTEREDITION_NS_URI = URI.create("http://interedition.eu/ns");

  final QName XML_ID_ATTR_NAME = new SimpleQName(XML_NS_URI, "id");

  final URI CLIX_NS = URI.create("http://lmnl.net/clix");
  final String CLIX_NS_PREFIX = "c";
  final QName CLIX_START_ATTR_NAME = new SimpleQName(CLIX_NS, "sID");
  final QName CLIX_END_ATTR_NAME = new SimpleQName(CLIX_NS, "eID");
}
