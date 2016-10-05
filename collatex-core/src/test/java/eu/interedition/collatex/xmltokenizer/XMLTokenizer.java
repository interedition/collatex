package eu.interedition.collatex.xmltokenizer;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XMLTokenizer {

    private Iterator<XMLEvent> iterator;
    private String xml;

    public XMLTokenizer(String xml) {
        this.xml = xml;
    }

    public Stream<XMLNode> getXMLNodeStream() {
        return eu.interedition.collatex.subst.XMLUtil.getXMLEventStream(xml)//
                .map(this::event2node)//
                .filter(Objects::nonNull);
    }

    private XMLNode event2node(XMLEvent xmlEvent) {
        switch (xmlEvent.getEventType()) {
        case XMLStreamConstants.START_ELEMENT:
            return startElementNode(xmlEvent);

        case XMLStreamConstants.CHARACTERS:
            return textNode(xmlEvent);

        case XMLStreamConstants.END_ELEMENT:
            return endElementNode(xmlEvent);

        default:
            return null;
        }

    }

    private XMLNode textNode(XMLEvent xmlEvent) {
        Characters characters = xmlEvent.asCharacters();
        String data = characters.getData();
        return new XMLTextNode(data);
    }

    private XMLNode startElementNode(XMLEvent xmlEvent) {
        StartElement startElement = xmlEvent.asStartElement();
        String qName = startElement.getName().getLocalPart();
        Map<String, String> attributes = attributes(startElement);
        return new XMLStartElementNode(qName, attributes);
    }

    private XMLNode endElementNode(XMLEvent xmlEvent) {
        EndElement endElement = xmlEvent.asEndElement();
        String qName = endElement.getName().getLocalPart();
        return new XMLEndElementNode(qName);
    }

    private Map<String, String> attributes(StartElement startElement) {
        Map<String, String> attributes = new LinkedHashMap<>();
        Iterator<Attribute> attributeIterator = startElement.getAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            QName qname = attribute.getName();
            String prefix = qname.getPrefix();
            String localPart = qname.getLocalPart();
            String name = prefix.isEmpty() ? localPart : prefix + ":" + localPart;
            attributes.put(name, attribute.getValue());
        }
        return attributes;
    }

}
