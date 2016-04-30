package eu.interedition.collatex.subst;

import eu.interedition.collatex.simple.SimplePatternTokenizer;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Function<String, Stream<String>> textTokenizer = SimplePatternTokenizer.BY_WS_OR_PUNCT;
        try {
            xmlStreamReader = xmlInputFactory.createXMLEventReader(new StringReader(xml_in));
            while (xmlStreamReader.hasNext()) {
                XMLEvent next = xmlStreamReader.nextEvent();
                if (next.isStartElement()) {
                    StartElement el = next.asStartElement();
                    String localName = el.getName().getLocalPart();
                    open_tags.add(localName);
                } else if (next.isCharacters()) {
                    Characters ch = next.asCharacters();
                    String text = ch.getData();
                    Stream<String> stringStream = textTokenizer.apply(text);
                    List<XMLToken> newTokens = stringStream.map(content -> new XMLToken(content, new ArrayList<>())).collect(Collectors.toList());
                    newTokens.get(0).getOpen_tags().addAll(open_tags);
                    tokens.addAll(newTokens);
                    open_tags = new ArrayList<>();
                } else if (next.isEndElement()) {
                    EndElement el = next.asEndElement();
                    String localName = el.getName().getLocalPart();
                    tokens.get(tokens.size()-1).addEndTag(localName);
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return tokens;
    }
}
