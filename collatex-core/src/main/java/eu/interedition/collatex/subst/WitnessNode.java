package eu.interedition.collatex.subst;

import com.google.common.collect.Maps;
import eu.interedition.collatex.simple.SimplePatternTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ronalddekker on 01/05/16.
 */
public class WitnessNode {

    private Integer rank = 0;

    enum Type {
        text, element
    }

    private WitnessNode parent;
    String data;
    private List<WitnessNode> children;
    private Type type;
    private String sigil;
    Map<String, String> attributes;

    public WitnessNode(String sigil, Type type, String data, Map<String, String> attributes, Integer rank) {
        this.sigil = sigil;
        this.type = type;
        this.data = data;
        this.attributes = attributes;
        this.rank = rank;
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

    public Integer getRank() {
        return rank;
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
        return depthFirstNodeEventStream(w -> true);
    }

    public Stream<WitnessNodeEvent> depthFirstNodeEventStream(Predicate<WitnessNode> node2ignorePredicate) {
        // NOTE: this if can be removed with inheritance, but that would mean virtual dispatch, so it is not a big win.
        if (this.children.isEmpty()) {
            return Stream.of(new WitnessNodeEvent(this, WitnessNodeEventType.TEXT));
        }

        boolean ignoreThisSubTree = node2ignorePredicate.test(this);
        if (ignoreThisSubTree) {
            return Stream.empty();
        }

        Stream<WitnessNodeEvent> a = Stream.of(new WitnessNodeEvent(this, WitnessNodeEventType.START));
        Stream<WitnessNodeEvent> b = children.stream()//
            .map(n -> n.depthFirstNodeEventStream(node2ignorePredicate)).flatMap(Function.identity());
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

    private static class XmlEventStreamContext {
        boolean ignoreXmlEvents = false;

        public boolean eventIsRelevant(XMLEvent xmlEvent) {
            boolean isRelevant = !ignoreXmlEvents;
            if (xmlEvent.isStartElement()) {
                if (isStartOfLitRdg(xmlEvent.asStartElement())) {
                    ignoreXmlEvents = true;
                    isRelevant = false;
                }
            } else if (xmlEvent.isEndElement()) {
                boolean isEndOfLitRdg = ignoreXmlEvents && xmlEvent.asEndElement().getName().toString().equals("rdg");
                if (isEndOfLitRdg) {
                    ignoreXmlEvents = false;
                    isRelevant = false;
                }
            }
            return isRelevant;
        }

        private static boolean isStartOfLitRdg(StartElement startElement) {
//        System.out.println("startElement="+startElement);
            Attribute typeAttribute = startElement.getAttributeByName(QName.valueOf("type"));
            return "rdg".equals(startElement.getName().toString())//
                && typeAttribute != null //
                && "lit".equals(typeAttribute.getValue());
        }
    }

    // this class creates a witness tree from a XML serialization of a witness
    // returns the root node of the tree
    public static WitnessNode createTree(String sigil, String witnessXML) {
        // use a stax parser to go from XML data to XML tokens
        WitnessNode initialValue = new WitnessNode(sigil, Type.element, "fake root", null, -1); // rank only needed for text nodes
        final AtomicReference<WitnessNode> currentNodeRef = new AtomicReference<>(initialValue);
        Function<String, Stream<String>> textTokenizer = SimplePatternTokenizer.BY_WS_OR_PUNCT;
        AtomicInteger rank = new AtomicInteger(0);
        Stack<Integer> rankStack = new Stack<>();
        Stack<Integer> highestChoiceStack = new Stack<>();
        XmlEventStreamContext context = new XmlEventStreamContext();
        XMLUtil.getXMLEventStream(witnessXML)//
            .filter(context::eventIsRelevant)//
            .forEach(xmlEvent -> {
                // System.out.println();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
//                    System.out.println("start element <" + startElement.getName().toString() + ">");
                    switch (startElement.getName().toString()) {
                        case "subst":
                        case "app":
                            rankStack.push(rank.get());
                            highestChoiceStack.push(rank.get());
                            break;
                        case "del":
                        case "add":
                        case "rdg":
                            rank.set(rankStack.peek());
                            break;
                    }
                    WitnessNode child = WitnessNode.fromStartElement(sigil, startElement);
                    currentNodeRef.get().addChild(child);
                    currentNodeRef.set(child);

                } else if (xmlEvent.isCharacters()) {
                    String text = xmlEvent.asCharacters().getData();
//                    System.out.println("text = " + text);
                    textTokenizer.apply(text)//
                        .map(s -> new WitnessNode(sigil, Type.text, s, new HashMap<>(), rank.getAndIncrement()))//
                        .collect(Collectors.toList())//
                        .forEach(currentNodeRef.get()::addChild);

                } else if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
//                    System.out.println("end element </" + endElement.getName().toString() + ">");
                    switch (endElement.getName().toString()) {
                        case "rdg":
                        case "del":
                        case "add":
                            Integer rankAtEndOfChoice = rank.get();
                            Integer currentHighestRankAtEndOfChoice = highestChoiceStack.pop();
                            highestChoiceStack.push(Math.max(rankAtEndOfChoice, currentHighestRankAtEndOfChoice));
                            rank.set(rankStack.peek());
                            break;
                        case "app":
                        case "subst":
                            rankStack.pop();
                            Integer newRank = highestChoiceStack.pop();
                            rank.set(newRank);
                            break;
                    }
                    currentNodeRef.set(currentNodeRef.get().parent);
                }

                // System.out.println("rank = " + rank.get());
                // System.out.println("rankStack.peek() = " + (rankStack.isEmpty() ? null : rankStack.peek()));
                // System.out.println("highestChoiceStack.peek() = " + (highestChoiceStack.isEmpty() ? null : highestChoiceStack.peek()));
            });

        return currentNodeRef.get().children.get(0);
    }


    private static WitnessNode fromStartElement(String sigil, StartElement startElement) {
        Map<String, String> attributes = Maps.newHashMap();
        startElement.getAttributes().forEachRemaining(a -> {
            Attribute attribute = (Attribute) a;
            attributes.put(attribute.getName().getLocalPart(), attribute.getValue());
        });
        String data2 = startElement.getName().getLocalPart();
        return new WitnessNode(sigil, Type.element, data2, attributes, -1);
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

        @Override
        public String toString() {
            return type.toString() + ": " + node.toString();
        }
    }

}
