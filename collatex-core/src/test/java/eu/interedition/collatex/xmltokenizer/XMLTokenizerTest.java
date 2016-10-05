package eu.interedition.collatex.xmltokenizer;

import org.junit.Test;

import eu.interedition.collatex.simple.SimplePatternTokenizer;

public class XMLTokenizerTest {
    @Test
    public void testXMLTokenizer1() {
        String xml = "<wit n=\"Wit1\">The "//
                + "<subst xml:id=\"subst-1\">"//
                + "<del hand=\"#AA\">white</del>"//
                + "<add hand=\"#AA\">black</add>"//
                + "</subst>"//
                + " god.</wit>";

        test(xml);
    }

    private void test(String xml) {
        LayeredTextTokenizer t = new LayeredTextTokenizer(xml, SimplePatternTokenizer.BY_WS_OR_PUNCT);

        for (LayeredTextToken token : t.getLayeredTextTokens()) {
            System.out.println(token);
            System.out.println(token.getAncestors());

        }
    }

    @Test
    public void testXMLTokenizer2() {
        String xml = "<wit n=\"Wit2\">The "//
                + "<!-- whatever --><app type=\"revision\">"//
                + "<rdg><del hand=\"#AA\">white &amp; purple, or blue and white</del></rdg>"//
                + "<rdg><add hand=\"#AA\">black</add></rdg>"//
                + "<rdg type=\"lit\"><del>white</del><hi rend=\"superscript\"><del>black</del></hi></rdg>"//
                + "</app>"//
                + " god.</wit>";

        test(xml);
    }

    @Test
    public void testXMLTokenizer3() {
        String xml = "<wit n=\"Wit3\">The"//
                + "<app type=\"revision\">"//
                + "<rdg><del hand=\"AA\">first</del></rdg>"//
                + "<rdg><add hand=\"AA\"><del hand=\"BB\">second</del></add></rdg>"//
                + "<rdg><add hand=\"BB\">third</add></rdg>"//
                + "<rdg type=\"lit\"><strike>first</strike><superscript><strike>second</strike></superscript><superscript>third</superscript></rdg>"//
                + "</app>"//
                + " attempt."//
                + "</wit>";

        test(xml);
    }

}
