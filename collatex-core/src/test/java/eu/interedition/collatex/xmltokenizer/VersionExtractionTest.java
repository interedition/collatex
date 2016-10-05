package eu.interedition.collatex.xmltokenizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.List;

public class VersionExtractionTest {
    @Test
    public void testAddAndDelToSeperateLayers() {
        String xml = "<wit n=\"Wit1\">The "//
                + "<subst xml:id=\"subst-1\">"//
                + "<del hand=\"#AA\">white</del>"//
                + "<add hand=\"#AA\">black</add>"//
                + "</subst>"//
                + " god.</wit>";

        VersionExtractor le = new VersionExtractor();
        List<String> versions = le.extractTextVersions(xml);
        assertThat(versions).hasSize(2);
        String v1 = versions.get(0);
        String v2 = versions.get(1);
        assertThat(v1).isEqualTo("The black god.");
        assertThat(v2).isEqualTo("The white god.");
    }

}
