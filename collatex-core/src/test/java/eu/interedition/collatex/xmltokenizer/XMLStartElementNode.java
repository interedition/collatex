package eu.interedition.collatex.xmltokenizer;

import java.util.Map;

public class XMLStartElementNode implements XMLNode {

    private String name;
    private Map<String, String> attributes;

    @Override
    public Type getType() {
        return Type.START_ELEMENT;
    }

    public XMLStartElementNode(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return XMLUtil.getOpenTag(name, attributes);
    }
}
