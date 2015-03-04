package eu.interedition.collatex.dekker;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.util.VariantGraphTraversal;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.*;

/**
 * Created by ronald on 2/27/15.
 */
public class VariantGraphMatcher extends BaseMatcher<VariantGraph> {

    class ExpectationTuple {
        private String[] expected_content; // expected token content in the normalized form
        private boolean aligned; // should the tokens be aligned in the graph or not

        public ExpectationTuple(String[] tokens, boolean aligned) {
            this.expected_content = tokens;
            this.aligned = aligned;
        }
    }

    private Witness w;
    private List<ExpectationTuple> expected = new ArrayList<>();
    private ExpectationTuple failed;

    private VariantGraphMatcher(Witness w) {
        this.w = w;
    }

    public static VariantGraphMatcher graph(Witness w) {
        return new VariantGraphMatcher(w);
    }

    public VariantGraphMatcher non_aligned(String... tokens) {
        expected.add(new ExpectationTuple(tokens, false));
        return this;
    }

    public VariantGraphMatcher aligned(String... tokens) {
        expected.add(new ExpectationTuple(tokens, true));
        return this;
    }

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof VariantGraph)) {
            return false;
        }
        VariantGraph g = (VariantGraph) item;
        VariantGraphTraversal graphTraversal = VariantGraphTraversal.of(g, Collections.singleton(w));
        Iterator<VariantGraph.Vertex> iterator = graphTraversal.iterator();
        iterator.next(); // skip start token
        for (ExpectationTuple expectation : expected) {
            for (String content : expectation.expected_content) {
                if (!iterator.hasNext()) {
                    failed = expectation;
                    return false;
                }
                VariantGraph.Vertex v = iterator.next();
                SimpleToken to = (SimpleToken) v.tokens().iterator().next();
                if (!(content.equals(to.getNormalized()))) {
                    failed = expectation;
                    return false;
                }
                // Check whether token is aligned or not
                if (expectation.aligned != v.tokens().size() > 1) {
                    failed = expectation;
                    return false;
                }
            }
        }
        // TODO: check more tokens than expected
        // TODO: skip end vertex
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("tokens ");
        for (ExpectationTuple expectation : expected) {
            for (String content : expectation.expected_content) {
                description.appendText(content);
                description.appendText(", ");
            }
        }
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if (failed.aligned) {
            description.appendText("aligned token(s): ");
        } else {
            description.appendText("non-aligned token(s): ");
        }
        description.appendText(Arrays.toString(failed.expected_content));
        description.appendText(" is/are missing!");
        // actual
        description.appendText("\nActual: ");
        VariantGraph g = (VariantGraph) item;
        VariantGraphTraversal graphTraversal = VariantGraphTraversal.of(g, Collections.singleton(w));
        Iterator<VariantGraph.Vertex> iterator = graphTraversal.iterator();
        iterator.next(); // skip start token
        while (iterator.hasNext()) {
            VariantGraph.Vertex v = iterator.next();
            if (v.tokens().size()>0) { // skip end token
                SimpleToken t = (SimpleToken) v.tokens().iterator().next();
                if (v.tokens().size()>1) {
                    description.appendText("aligned: " + t.getNormalized().toString()+", ");
                } else {
                    description.appendText("non-aligned: "+ t.getNormalized().toString()+", ");
                }
            }
        }
    }
}
