package eu.interedition.collatex.dekker;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.util.VariantGraphTraversal;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Iterator;

/**
 * Created by ronald on 2/26/15.
 */
public class VariantGraphTraversalMatcher extends BaseMatcher<VariantGraphTraversal> {
    private final String[] token_content;

    public static Matcher<VariantGraphTraversal> non(String... token_content) {
        return new VariantGraphTraversalMatcher(token_content);
    }

    VariantGraphTraversalMatcher(String[] token_content) {
        this.token_content = token_content;
    }

    @Override
    public boolean matches(Object o) {
        if (!(o instanceof VariantGraphTraversal)) {
            return false;
        }
        VariantGraphTraversal t = (VariantGraphTraversal) o;
        Iterator<VariantGraph.Vertex> iterator = t.iterator();
        iterator.next(); // skip start token
        for (String content: token_content) {
            if (!iterator.hasNext()) {
                return false;
            }
            VariantGraph.Vertex v = iterator.next();
            SimpleToken to = (SimpleToken) v.tokens().iterator().next();
            if (!(content.equals(to.getNormalized()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("");
        for (String content: token_content) {
            description.appendText(content);
            description.appendText(",");
        }
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if (!(item instanceof VariantGraphTraversal)) {
            super.describeMismatch(item, description);
            return;
        }
        VariantGraphTraversal t = (VariantGraphTraversal) item;
        Iterator<VariantGraph.Vertex> iterator = t.iterator();
        for (VariantGraph.Vertex v; iterator.hasNext(); ) {
            v = iterator.next();
            // skip start/end vertices
            if (v.tokens().isEmpty()) {
                continue;
            }
            SimpleToken to = (SimpleToken) v.tokens().iterator().next();
            description.appendText(to.getNormalized());
            description.appendText(",");
        }
    }
}
