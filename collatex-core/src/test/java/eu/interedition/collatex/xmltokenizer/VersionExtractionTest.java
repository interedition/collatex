package eu.interedition.collatex.xmltokenizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
        assertThat(v1).isEqualTo("The white god.");
        assertThat(v2).isEqualTo("The black god.");
    }

    @Test
    public void testVersionsFromExamples() throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream("testwitnesses.xml");
        BufferedReader in = new BufferedReader(new InputStreamReader(resourceAsStream));

        List<String> witnessSources = new ArrayList<>();

        String line = null;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("<wit ")) {
                witnessSources.add(line);
            }
        }

        VersionExtractor le = new VersionExtractor();
        witnessSources.forEach(xml -> {
            List<String> versions = le.extractTextVersions(xml);
            System.out.println();
            System.out.println("Original: " + xml);
            System.out.println("Layers: ");
            versions.forEach(System.out::println);
        });
    }

}
