package eu.interedition.collatex.subst;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronalddekker on 30/04/16.
 */
public class SubstTokenizer {
    private String xml_in;

    public SubstTokenizer(String xml_in) {
        this.xml_in = xml_in;
    }

    public List<XMLToken> tokenize() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlStreamReader;
        List<XMLToken> tokens = new ArrayList<>();
        List<String> open_tags = new ArrayList<>();
        try {
            xmlStreamReader = xmlInputFactory.createXMLEventReader(new StringReader(xml_in));
            while (xmlStreamReader.hasNext()) {
                XMLEvent next = xmlStreamReader.nextEvent();
                if (next.isStartElement()) {
                    StartElement el = next.asStartElement();
                    String localName = el.getName().getLocalPart();
                    //System.out.println(localName);
                    open_tags.add(localName);
                } else if (next.isCharacters()) {
                    Characters ch = next.asCharacters();
                    String text = ch.getData();
                    XMLToken token = new XMLToken(text, open_tags);
                    tokens.add(token);
                    open_tags = new ArrayList<>();
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return tokens;

    }
}
