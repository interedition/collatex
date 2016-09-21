package eu.interedition.collatex.subst;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import eu.interedition.collatex.AbstractTest;

public class ESTSUseCases extends AbstractTest {
    // @Ignore
    @Test
    public void testStandardSubstitution1() {
        String wit1 = "<wit n=\"Wit1\">The "//
                + "<subst>"//
                + "<del hand=\"#AA\">white</del>"//
                + "<add hand=\"#AA\">black</add>"//
                + "</subst>"//
                + " god.</wit>";
        standardSubstitutionTest(wit1);
    }

    @Ignore
    @Test
    public void testStandardSubstitution2() {
        String wit1 = "<wit n=\"Wit1\">The "//
                + "<app type=\"revision\">"//
                + "<rdg><del hand=\"#AA\">white</del></rdg>"//
                + "<rdg><add hand=\"#AA\">black</add></rdg>"//
                + "<rdg type=\"lit\"><del>white</del><hi rend=\"superscript\"><del>black</del></hi></rdg>"//
                + "</app>"//
                + " god.</wit>";
        standardSubstitutionTest(wit1);
    }

    private void standardSubstitutionTest(String wit1) {
        String wit2 = "<wit n=\"Wit2\">The black god.</wit>";
        String output = "The "//
                + "<app>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"0\"><del hand=\"#AA\">white</del></rdg>"//
                + "<rdgGrp type=\"tag_variation_only\">"//
                + "<rdg wit=\"#Wit1\" varSeq=\"1\"><add hand=\"#AA\">black</add></rdg>"//
                + "<rdg wit=\"#Wit2\">black</rdg>"//
                + "</rdgGrp>"//
                + "</app> god.";

        verifyCollationTEI(wit1, wit2, output);
    }

    @Ignore
    @Test
    public void testSubstitutionWithinSubstitution1() {
        String wit1 = "<wit n=\"Wit1\">The "//
                + "<subst>"//
                + "<del hand=\"#AA\">first</del>"//
                + "<add hand=\"#AA\">"//
                + "<subst>"//
                + "<del hand=\"#BB\">second</del>"//
                + "<add hand=\"#BB\">third</add>"//
                + "</subst>"//
                + "</add>"//
                + "</subst>"//
                + "attempt.</wit>";
        substitutionWithinSubstitutionTest(wit1);
    }

    private void substitutionWithinSubstitutionTest(String wit1) {
        String wit2 = "<wit n=\"Wit2\">The second attempt</wit>";
        String output = "The "//
                + "<app>"//

                + "<rdg wit=\"#Wit1\" varSeq=\"0\"><del hand=\"#AA\">first</del></rdg>"//

                + "<rdgGrp type=\"tag_variation_only\">"//
                + "<rdg wit=\"#Wit1\" varSeq=\"1\">"//
                + "<add hand=\"#AA\">"//
                + "<del hand=\"#BB\">second</del>"//
                + "</add>"//
                + "</rdg>"//

                + "<rdg wit=\"#Wit2\">second</rdg>"//
                + "</rdgGrp>"//

                + "<rdg wit=\"#Wit1\" varSeq=\"2\"><add hand=\"#BB\"><add hand=\"#BB\">third</add></add></rdg>"//

                + "</app>"//
                + "attempt.";
        verifyCollationTEI(wit1, wit2, output);
    }

    private void verifyCollationTEI(String wit1, String wit2, String expectedTEI) {
        String collationTei = collate2tei(wit1, wit2);
        String wrapped = "<?xml version=\"1.0\" ?><apparatus>" + expectedTEI + "</apparatus>";
        assertEquals(wrapped, collationTei);
    }

    private String collate2tei(String w1, String w2) {
        WitnessNode a = WitnessNode.createTree("Wit1", w1);
        WitnessNode b = WitnessNode.createTree("Wit2", w2);
        EditGraphAligner aligner = new EditGraphAligner(a, b);
        List<List<WitnessNode>> superWitness = aligner.getSuperWitness();

        XMLOutput xmlOutput = new XMLOutput(superWitness);
        StringWriter writer = new StringWriter();
        try {
            xmlOutput.printXML(writer);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

}
