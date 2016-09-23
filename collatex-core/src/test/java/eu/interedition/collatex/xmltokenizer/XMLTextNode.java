package eu.interedition.collatex.xmltokenizer;

public class XMLTextNode implements XMLNode {

    private String text;

    public XMLTextNode(String text) {
        this.text = text;
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "\"" + text + "\"";
    }
}
