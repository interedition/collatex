package eu.interedition.collatex.dekker;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.util.VariantGraphTraversal;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronald on 2/27/15.
 */
public class VariantGraphMatcher extends BaseMatcher<VariantGraph> {

    private Witness w;
    private List<Matcher> matchers = new ArrayList<>();
    private Matcher failedMatcher;

    private VariantGraphMatcher(Witness w) {
        this.w = w;
    }

    public static VariantGraphMatcher graph(Witness w) {
        return new VariantGraphMatcher(w);
    }

    public VariantGraphMatcher non_aligned(String... tokens) {
        // should become iterator instead of VGT
        this.matchers.add(new VariantGraphTraversalMatcher(tokens));
        return this;
    }

    public VariantGraphMatcher aligned(String... tokens) {
        return this;
    }

    @Override
    public boolean matches(Object item) {
        VariantGraph v = (VariantGraph) item;
        VariantGraphTraversal graphTraversal = VariantGraphTraversal.of(v);
        for (Matcher m : matchers) {
            if (!m.matches(graphTraversal)) {
                failedMatcher = m;
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        for (Matcher m: matchers) {
            description.appendDescriptionOf(m);
        }

    }

    @Override
    public void describeMismatch(Object item, Description description) {
        VariantGraphTraversal traversal = VariantGraphTraversal.of((VariantGraph) item);
        failedMatcher.describeMismatch(traversal, description);
    }
}
