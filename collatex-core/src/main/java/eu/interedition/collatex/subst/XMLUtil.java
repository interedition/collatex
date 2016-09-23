package eu.interedition.collatex.subst;

import java.io.StringReader;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class XMLUtil {
    public static Stream<XMLEvent> getXMLEventStream(String xml) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(xml));
            Iterable<XMLEvent> iterable = () -> new XMLEventIterator(xmlEventReader);
            return StreamSupport.stream(iterable.spliterator(), false);

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }

    }

    static class XMLEventIterator implements Iterator<XMLEvent> {

        private XMLEventReader eventReader;

        public XMLEventIterator(XMLEventReader eventReader) {
            this.eventReader = eventReader;
        }

        @Override
        public boolean hasNext() {
            return this.eventReader.hasNext();
        }

        @Override
        public XMLEvent next() {
            try {
                return this.eventReader.nextEvent();
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
