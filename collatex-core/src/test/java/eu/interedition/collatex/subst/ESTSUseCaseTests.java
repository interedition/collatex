package eu.interedition.collatex.subst;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import eu.interedition.collatex.AbstractTest;

public class ESTSUseCaseTests extends AbstractTest {
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
    public void testStandardSubstitution3() {
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
    public void testStandardSubstitutionA1() {
        String wit1 = "<wit n=\"Wit1\">bench by the "//
                + "<subst>"//
                + "<del hand=\"#SB\">lock</del>"//
                + "<add hand=\"#SB\">weir</add>"//
                + "</subst>"//
                + "</wit>";
        standardSubstitutionTestA(wit1);
    }

    @Ignore
    @Test
    public void testStandardSubstitutionA3() {
        String wit1 = "<wit n=\"Wit1\">bench by the "//
                + "<app>"//
                + "<rdg type=\"deletion\"><del hand=\"#SB\">lock</del></rdg>"//
                + "<rdg type=\"addition\"><add hand=\"#SB\">weir</add></rdg>"//
                + "<rdg type=\"lit\"><hi rend=\"strike\">lock</hi> <hi rend=\"sup\">weir</hi></rdg>"//
                + "</app>"//
                + "</wit>";
        standardSubstitutionTestA(wit1);
    }

    private void standardSubstitutionTestA(String wit1) {
        String wit2 = "<wit n=\"Wit2\">bench by the weir</wit>";
        String output = "bench by the "//
                + "<app>"//
                + "<rdg wit=\"#Wit1\" type=\"deletion\" varSeq=\"0\"><del hand=\"#SB\">lock</del></rdg>"//
                + "<rdgGrp type=\"tag_variation_only\">"//
                + "<rdg wit=\"#Wit1\" type=\"addition\" varSeq=\"1\"><add hand=\"#SB\">weir</add></rdg>"//
                + "<rdg wit=\"#Wit2\">weir</rdg>"//
                + "</rdgGrp>"//
                + "</app>";

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
                + " attempt.</wit>";
        substitutionWithinSubstitutionTest(wit1);
    }

    @Ignore
    @Test
    public void testSubstitutionWithinSubstitution3() {
        String wit1 = "<wit n=\"Wit1\">The"//
                + "<app type=\"revision\">"//
                + "<rdg><del hand=\"AA\">first</del></rdg>"//
                + "<rdg><add hand=\"AA\"><del hand=\"BB\">second</del></add></rdg>"//
                + "<rdg><add hand=\"BB\">third</add></rdg>"//
                + "<rdg type=\"lit\"><strike>first</strike><superscript><strike>second</strike></superscript><superscript>third</superscript></rdg>"//
                + "</app>"//
                + " attempt."//
                + "</wit>";
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

    @Ignore
    @Test
    public void testSubstitutionWithinAWord1() {
        String wit1 = "<wit n=\"Wit1\">Th<subst><del>is</del><add>e</add></subst> dog.</wit>";
        substitutionWithinAWordTest(wit1);
    }

    @Ignore
    @Test
    public void testSubstitutionWithinAWord3() {
        String wit1 = "<wit n=\"Wit1\">"//
                + "<app type=\"revision\">"//
                + "<rdg>This</rdg>"//
                + "<rdg>The</rdg>"//
                + "<rdg type=\"lit\">Th<del>is</del><add>e</add></rdg>"//
                + "</app>"//
                + " dog.</wit>";
        substitutionWithinAWordTest(wit1);
    }

    private void substitutionWithinAWordTest(String wit1) {
        String wit2 = "<wit n=\"Wit2\">The dog</wit>";
        String output = "Th"//
                + "<app>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"0\"><del hand=\"#AA\">is</del>v</rdg>"//
                + "<rdgGrp type=\"tag_variation_only>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"1\"><add hand=\"#AA\">e</add></rdg>"//
                + "<rdg wit=\"#Wit2\">e</rdg>"//
                + "</rdgGrp>"//
                + "</app>"//
                + " dog.";

        String output2 = "<app>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"0\"><del hand=\"#AA\">This</del></rdg>"//
                + "<rdgGrp type=\"tag_variation_only\">"//
                + "<rdg wit=\"#Wit1\" varSeq=\"1\"><add hand=\"#AA\">The</add></rdg>"//
                + "<rdg wit=\"#Wit2\">The</rdg>"//
                + "</rdgGrp>"//
                + "</app>"//
                + " dog.";
        verifyCollationTEI(wit1, wit2, output);
    }

    @Ignore
    @Test
    public void testAlternativeReading1() {
        String wit1 = "<wit n=\"Wit1\">"//
                + "The <seg xml:id=\"alt1\">leather</seg> <add xml:id=\"alt2\">parchment</add> page."//
                + "<alt target=\"#alt1 #alt2\"/></wit>";
        alternativeReadingTest(wit1);
    }

    @Ignore
    @Test
    public void testAlternativeReading3a() {
        String wit1 = "<wit n=\"Wit1\">"//
                + "The"//
                + "<app type=\"revision\">"//
                + "<rdg type=\"\"><seg type=\"alternative\">leather</seg></rdg>"//
                + "<rdg type=\"\"><add type=\"alternative\">parchment</add></rdg>"//
                + "<rdg type=\"lit\">leather <hi rend=\"superscript\">parchment</hi></rdg>"//
                + "</app>"//
                + "page.</wit>";
        alternativeReadingTest(wit1);
    }

    @Ignore
    @Test
    public void testAlternativeReading3b() {
        String wit1 = "<wit n=\"Wit1\">"//
                + "<app>"//
                + "<rdg><seg type=\"alternative\">leather</seg></rdg>"//
                + "<rdg><add type=\"alternative\">parchment</add></rdg>"//
                + "<rdg type=\"lit\">leather<hi rend=\"superscript\">parchment</hi></rdg>"//
                + "</app>"//
                + " page."//
                + "</wit>";
        alternativeReadingTest(wit1);
    }

    private void alternativeReadingTest(String wit1) {
        String wit2 = "<wit n=\"Wit2\">The parchment page.</wit>";
        String output = "The "//
                + "<app>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"0\">"//
                + "<seg type=\"alternative\">leather</seg>"//
                + "</rdg>"//
                + "<rdgGrp type=\"tag_variation_only\">"//
                + "<rdg wit=\"#Wit1\" varSeq=\"1\">"//
                + "<add type=\"alternative\">parchment</add>"//
                + "</rdg>"//
                + "<rdg wit=\"#Wit2\">parchment</rdg>"//
                + "</rdgGrp>"//
                + "</app>"//
                + " page.";
        verifyCollationTEI(wit1, wit2, output);
    }

    @Ignore
    @Test
    public void testLongSubstitutions1() {
        String wit1 = "<wit n=\"Wit1\">The "//
                + "<subst>"//
                + "<del hand=\"#AA\">big black ears</del>"//
                + "<add hand=\"#AA\">brown eyes</add>"//
                + "</subst>"//
                + " of the dog.</wit>";
        longSubstitutionTest(wit1);
    }

    @Ignore
    @Test
    public void testLongSubstitutions3() {
        String wit1 = "<wit n=\"Wit1\">The "//
                + "<app type=\"revision\">"//
                + "<rdg varSeq=\"0\"><del hand=\"#AA\">big black ears</del></rdg>"//
                + "<rdg varSeq=\"1\"><add hand=\"#AA\">brown eyes</add></rdg>"//
                + "<rdg type=\"lit\"><del>big black ears</del><hi rend=\"superscript\"><del>brown eyes</del></hi></rdg>"//
                + "</app>"//
                + " of the dog.</wit>";
        longSubstitutionTest(wit1);
    }

    // BB: doesn't putting black/brown and ears/eyes in the same app require knowledge of the type of words?
    private void longSubstitutionTest(String wit1) {
        String wit2 = "<wit n=\"Wit2\">The big eyes of the dog.</wit>";
        // matching words is dominant
        String output = "The "//
                + "<app>"//
                + "<rdgGrp type=\"tag_variation_only\">"//
                + "<rdg wit=\"#Wit1\" varSeq=\"0\"><del hand=\"#AA\">big</del></rdg>"//
                + "<rdg wit=\"#Wit2\">big</rdg>"//
                + "</rdgGrp>"//
                + "</app>"//

                + "<app>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"0\"><del hand=\"#AA\">black</del></rdg>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"1\"><add hand=\"#AA\">brown</add></rdg>"//
                + "<rdg wit=\"#Wit2\"></rdg>"//
                + "</app>"//

                + "<app>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"0\"><del hand=\"#AA\">ears</del></rdg>"//
                + "<rdgGrp type=\"tag_variation_only\">"//
                + "<rdg wit=\"#Wit1\" varSeq=\"1\"><add hand=\"#AA\">eyes</add></rdg>"//
                + "<rdg wit=\"#Wit2\">eyes</rdg>"//
                + "</rdgGrp>"//
                + "</app>"//

                + " of the dog.";
        verifyCollationTEI(wit1, wit2, output);

        // substitution is dominant
        String output2 = "The "//
                + "<app>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"0\"><del>big black ears</del></rdg>"//
                + "<rdg wit=\"#Wit1\" varSeq=\"1\"><add>brown eyes</add></rdg>"//
                + "<rdg wit=\"#Wit2\">big eyes</rdg>"//
                + "</app>"//
                + " of the dog.";
        verifyCollationTEI(wit1, wit2, output2);
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
