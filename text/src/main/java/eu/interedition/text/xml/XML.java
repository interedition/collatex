package eu.interedition.text.xml;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.stream.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XML {
  public static XMLInputFactory2 createXMLInputFactory() {
    final XMLInputFactory2 xmlInputFactory = (XMLInputFactory2) XMLInputFactory2.newFactory();
    xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
    xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
    xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
    return xmlInputFactory;
  }

  public static XMLOutputFactory2 createXMLOutputFactory() {
    final XMLOutputFactory2 xmlOutputFactory = (XMLOutputFactory2) XMLOutputFactory2.newFactory();
    xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
    return xmlOutputFactory;
  }

  public static void closeQuietly(XMLStreamReader reader) {
    try {
      if (reader != null) {
        reader.close();
      }
    } catch (XMLStreamException e) {
    }
  }

  public static void closeQuietly(XMLEventReader reader) {
    try {
      if (reader != null) {
        reader.close();
      }
    } catch (XMLStreamException e) {
    }
  }

  public static void closeQuietly(XMLStreamWriter writer) {
    try {
      if (writer != null) {
        writer.close();
      }
    } catch (XMLStreamException e) {
    }
  }

  public static void closeQuietly(XMLEventWriter writer) {
    try {
      if (writer != null) {
        writer.close();
      }
    } catch (XMLStreamException e) {
    }
  }
}
