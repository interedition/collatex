package eu.interedition.collatex.xmltokenizer;

import static java.util.stream.Collectors.toList;

import java.util.Deque;
import java.util.List;

public class LayeredTextToken {

    private String sigil;
    private String tokenText;
    private List<XMLStartElementNode> ancestors;

    public LayeredTextToken(String sigil, String tokenText, Deque<XMLStartElementNode> openedElements) {
        this.sigil = sigil;
        this.tokenText = tokenText;
        this.ancestors = openedElements.stream().collect(toList());
    }

    public String getSigil() {
        return this.sigil;
    }

    public String getTokenText() {
        return this.tokenText;
    }

    public String getNormalizedText() {
        return this.tokenText.toLowerCase().trim();
    }

    public List<XMLStartElementNode> getAncestors() {
        return this.ancestors;
    }

    @Override
    public String toString() {
        return "#" + this.sigil + " [" + this.tokenText + "|" + getNormalizedText() + "]";
    }
}
