package eu.interedition.collatex.xmltokenizer;

public interface XMLNode {

    enum Type {
        START_ELEMENT, TEXT, END_ELEMENT
    }

    public Type getType();
}
