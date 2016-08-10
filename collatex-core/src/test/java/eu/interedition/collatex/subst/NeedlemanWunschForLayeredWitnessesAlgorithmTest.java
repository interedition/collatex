package eu.interedition.collatex.subst;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

public class NeedlemanWunschForLayeredWitnessesAlgorithmTest extends AbstractTest {
    @Before
    public void before() {
        setCollationAlgorithm(new NeedlemanWunschForLayeredWitnessesAlgorithm());
    }

    @Test
    public void testCollation1() throws Exception {
        String xml_a = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\">In <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";
        String xml_c = "<wit n=\"3\"><subst><del>Since</del><add>From</add></subst> <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";

        Iterable<Token> witnessA = tokenize(xml_a);
        Iterable<Token> witnessB = tokenize(xml_b);
        Iterable<Token> witnessC = tokenize(xml_c);
        final VariantGraph graph = new VariantGraph();
        collationAlgorithm.collate(graph, witnessA, witnessB, witnessC);
        LOG.fine(toString(table(graph)));
    }

    private Iterable<Token> tokenize(String xml) {
        return null;
    }

}
