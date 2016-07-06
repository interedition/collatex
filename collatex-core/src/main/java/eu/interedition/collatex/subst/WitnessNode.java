package eu.interedition.collatex.subst;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import eu.interedition.collatex.simple.SimplePatternTokenizer;

/**
 * Created by ronalddekker on 01/05/16.
 */
public class WitnessNode {
    enum Type {
        text, element
    }

    private WitnessNode parent;
    String data;
    private List<WitnessNode> children;
    private Type type;
    private String sigil;

    public WitnessNode(String sigil, Type type, String data) {
        this.sigil = sigil;
        this.type = type;
        this.data = data;
        this.children = new ArrayList<>();
    }

    public void addChild(WitnessNode child) {
        children.add(child);
        child.parent = this;
    }

    public WitnessNode getLastChild() {
        if (children.isEmpty()) {
            throw new RuntimeException("There are no children!");
        }
        return children.get(children.size() - 1);
    }

    @Override
    public String toString() {
        return data;
    }

    public Stream<WitnessNode> children() {
        return this.children.stream();
    }

    // traverses recursively
    public Stream<WitnessNode> depthFirstNodeStream() {
        return Stream.concat(Stream.of(this), children.stream().map(WitnessNode::depthFirstNodeStream).flatMap(Function.identity()));
    }

    // traverses recursively; only instead of nodes we return events
    public Stream<WitnessNodeEvent> depthFirstNodeEventStream() {
        // NOTE: this if can be removed with inheritance, but that would mean virtual dispatch, so it is not a big win.
        if (this.children.isEmpty()) {
            return Stream.of(new WitnessNodeEvent(this, WitnessNodeEventType.TEXT));
        }

        Stream<WitnessNodeEvent> a = Stream.of(new WitnessNodeEvent(this, WitnessNodeEventType.START));
        Stream<WitnessNodeEvent> b = children.stream().map(WitnessNode::depthFirstNodeEventStream).flatMap(Function.identity());
        Stream<WitnessNodeEvent> c = Stream.of(new WitnessNodeEvent(this, WitnessNodeEventType.END));
        return Stream.concat(a, Stream.concat(b, c));
    }

    public Stream<WitnessNode> parentNodeStream() {
        // note: this implementation is not lazy
        List<WitnessNode> parents = new ArrayList<>();
        WitnessNode current = this.parent;
        while (current != null) {
            parents.add(current);
            current = current.parent;
        }
        return parents.stream();
    }

    // NOTE: this implementation should be faster, but it does not work
    // if (this.parent == null) {
    // return Stream.empty();
    // }
    // Stream a = Stream.of(this.parent);
    // Stream b = Stream.of(this.parent.parentNodeStream());
    // return Stream.concat(a, b);
    // }

    // this class creates a witness tree from a XML serialization of a witness
    // returns the root node of the tree
    public static WitnessNode createTree(String sigil, String witnessXML) {
        // use a stax parser to go from XML data to XML tokens
        // NOTE: there is no implementation of Stream API for STAX
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlStreamReader;
        WitnessNode currentNode = new WitnessNode(sigil, Type.element, "fake root");
        Function<String, Stream<String>> textTokenizer = SimplePatternTokenizer.BY_WS_OR_PUNCT;
        try {
            xmlStreamReader = xmlInputFactory.createXMLEventReader(new StringReader(witnessXML));
            while (xmlStreamReader.hasNext()) {
                XMLEvent next = xmlStreamReader.nextEvent();
                if (next.isStartElement()) {
                    String localName = next.asStartElement().getName().getLocalPart();
                    WitnessNode child = new WitnessNode(sigil, Type.element, localName);
                    currentNode.addChild(child);
                    currentNode = child;
                } else if (next.isCharacters()) {
                    String text = next.asCharacters().getData();
                    textTokenizer.apply(text)//
                            .map(s -> new WitnessNode(sigil, Type.text, s))//
                            .collect(Collectors.toList())//
                            .forEach(currentNode::addChild);
                } else if (next.isEndElement()) {
                    currentNode = currentNode.parent;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return currentNode.children.get(0);
    }

    public Type getType() {
        return type;
    }

    public boolean isElement() {
        return Type.element.equals(type);
    }

    public String getSigil() {
        return sigil;
    }

    public enum WitnessNodeEventType {
        START, END, TEXT
    }

    static class WitnessNodeEvent {
        protected WitnessNode node;
        protected WitnessNodeEventType type;

        public WitnessNodeEvent(WitnessNode node, WitnessNodeEventType type) {
            this.node = node;
            this.type = type;
        }
    }

}
