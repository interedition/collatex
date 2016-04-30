package eu.interedition.collatex.subst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronalddekker on 30/04/16.
 */
public class XMLToken {
    private final String text;
    private final List<String> open_tags;
    private final List<String> end_tags;

    public XMLToken(String text, List<String> open_tags) {
        this.text = text;
        this.open_tags = open_tags;
        this.end_tags = new ArrayList<>();
    }

    public String getText() {
        return text;
    }

    public List<String> getOpen_tags() {
        return open_tags;
    }

    public List<String> getEnd_tags() {
        return end_tags;
    }

    public void addEndTag(String localName) {
        end_tags.add(localName);
    }
}
