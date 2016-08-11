package eu.interedition.collatex.subst;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
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

        collate(xml_a, xml_b, xml_c);
    }

    @Test
    public void testCollation2() throws Exception {
        String xml_a = "<wit n=\"1\">It's not an easy thing at all to do.</wit>";
        String xml_b = "<wit n=\"2\">It <subst><del>is not easy</del><add>gets</add></subst> <subst><del>at all</del><add>all very complicated</add></subst>.</wit>";
        String xml_c = "<wit n=\"3\">It all gets very complicated.</wit>";

        collate(xml_a, xml_b, xml_c);
    }

    private void collate(String xml_a, String xml_b, String xml_c) {
        LayeredWitness witnessA = new LayeredWitness("1", xml_a);
        LayeredWitness witnessB = new LayeredWitness("2", xml_b);
        LayeredWitness witnessC = new LayeredWitness("3", xml_c);
        final VariantGraph graph = new VariantGraph();
        collationAlgorithm.collate(graph, witnessA, witnessB, witnessC);
        LOG.fine(toString(table(graph)));
    }

}
