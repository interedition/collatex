package eu.interedition.collatex.xmltokenizer;

public class XMLEndElementNode implements XMLNode {

    private String name;

    public XMLEndElementNode(String name) {
        this.name = name;
    }

    @Override
    public Type getType() {
        return Type.END_ELEMENT;
    }

    @Override
    public String toString() {
        return XMLUtil.getCloseTag(this.name);
    }

}
