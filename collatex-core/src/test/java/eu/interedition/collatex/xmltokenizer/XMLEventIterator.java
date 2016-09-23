package eu.interedition.collatex.xmltokenizer;

import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class XMLEventIterator implements Iterator<XMLEvent> {

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
