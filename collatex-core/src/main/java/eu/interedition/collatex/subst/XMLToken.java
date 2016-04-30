package eu.interedition.collatex.subst;

import java.util.List;

/**
 * Created by ronalddekker on 30/04/16.
 */
public class XMLToken {
    private final String text;
    private final List<String> open_tags;

    public XMLToken(String text, List<String> open_tags) {

        this.text = text;
        this.open_tags = open_tags;
    }

    public String getText() {
        return text;
    }

    public List<String> getOpen_tags() {
        return open_tags;
    }

}
