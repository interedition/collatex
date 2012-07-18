package eu.interedition.collatex.simple;

import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Witness;

// we are going to use stax
// previous version was build on DOM, which is not particularly well suited to parsing
// large TEI files, with segments and expansions
public class SimpleWitnessTeiBuilder {

  private static QName w = new QName("http://www.tei-c.org/ns/1.0", "w");

  public SimpleWitness read(InputStream input) throws XMLStreamException {
    SimpleWitness witness = new SimpleWitness("id");
    List<String> tokenContents = Lists.newArrayList();
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLEventReader reader = factory.createXMLEventReader(input);
    XMLEvent event = null;
    while ((event = reader.peek()) != null) {
//      System.out.println(event.toString());
      if (event.isStartElement() && event.asStartElement().getName().equals(w)) {
	tokenContents.add(handleWElement(reader));
      } else {
	reader.next();
      }
    }
    witness.setTokenContents(tokenContents);
    return witness;
  }

  private static String handleWElement(XMLEventReader reader) throws XMLStreamException {
    XMLEvent event = reader.nextEvent();
    // Do what you need to do with the start element, e.g. initialize
    // data structures
    //System.out.println("W tag is triggered here!");
    StringBuffer textBuffer = new StringBuffer();
    while ((event = reader.peek()) != null) {
      if (event.isEndElement() && event.asEndElement().getName().equals(w)) {
	// Do what you need to do at the end, e.g. add data
	// collected from sub elements, etc.
	event = reader.nextEvent();
	break;
      } else {
	// Do what you need to do for start or child elements, e.g.
	// dispatch to another handler function
	event = reader.nextEvent();
	textBuffer.append(event.toString());
	//System.out.println("Text :"+event.toString());
      }
    }
    return textBuffer.toString();
  }

}
