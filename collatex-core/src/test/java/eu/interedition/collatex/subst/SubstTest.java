package eu.interedition.collatex.subst;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthLongestWord;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleToken;

public class SubstTest extends AbstractTest {
    Logger LOG = Logger.getLogger(this.getClass().getName());

    List<List<String>> examples = asList(//
            asList(// een voorbeeld met enkele substituties van woorden, genoeg variatie tussen de witnesses om CX op de proef mee te stellen.
                    "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>", //
                    "<wit n=\"2\">In <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>", //
                    "<wit n=\"3\">At the outset, looking for the best word.</wit>"//
            ), asList(
                    // voorbeeld met variatie binnen een woord, optie 1. Hier heb ik subst rond het volledige woord gezet. De platte tekst binnen de <subst> (in dit geval \"Th\") moet meegenomen
                    // worden in het ...OF... verhaal: The OF This.
                    "<wit n=\"1\"><subst>Th<del>e</del><add>is</add></subst> is an example with an edit within a word.</wit>", //
                    "<wit n=\"2\">This is a simple example with an edit within a word.</wit>" //
            ), asList(// voorbeeld met variatie binnen een woord, optie 2. Een <app> tagging splitst vooraf al op in subwitnesses. De reading witness \"transcription\" moet genegeerd worden in de
                      // collatie.
                    "<wit n=\"1\"><app><rdg wit=\"collation_A\">The</rdg><rdg wit=\"collation_B\">This</rdg><rdg wit=\"transcription\">Th<del>e</del><add>is</add></rdg></app> is an example with an edit within a word.</wit>", //
                    "<wit n=\"2\">This is a simple example with an edit within a word.</wit>" //
            ), asList(// voorbeeld met variatie binnen een woord, optie 3. Hetzelfde principe als optie 2, maar met de extra info in de readings van welke letters er precies is geschrapt, toegevoegd
                    "<wit n=\"1\"><app><rdg wit=\"collation_A\">Th<del>e</del></rdg><rdg wit=\"collation_B\">Th<add>is</add></rdg><rdg wit=\"transcription\">Th<del>e</del><add>is</add></rdg></app> is an example with an edit within a word.</wit>", //
                    "<wit n=\"2\">This is a simple example with an edit within a word.</wit>" //
            ), asList(// een voorbeeld met een langere substitutie.
                    "<wit n=\"1\">It is not an easy thing at all with long substitutions.</wit>", //
                    "<wit n=\"2\">It <subst><del>is not easy at all</del><add>all gets complicated</add></subst> with long substitutions.</wit>", //
                    "<wit n=\"3\">It all gets very complicated with a lot of substitutions.</wit>"//
            ), asList(// een variant hiervan met meerdere langere substituties, waaronder een substitutie waar geen <del> in zit, alleen een <add>.
                    "<wit n=\"1\">It is not an easy thing at all with long substitutions.</wit>", //
                    "<wit n=\"2\">It <subst><del>is not</del><add>gets</add></subst> <subst><del>easy</del><add>complicated</add></subst> <subst><add>very quickly</add></subst> with long substitutions.</wit>", //
                    "<wit n=\"3\">It all gets very complicated with a lot of substitutions.</wit>"//
            ), asList(// een andere optie: vooraf in subversies onderverdeeld. Makkelijker voor CX waarschijnlijk, maar er gaat wat informatie verloren: waar precies de substituties zitten.
                    "<wit n=\"1\">It is not an easy thing at all with long substitutions.</wit>", //
                    "<wit n=\"2\">It <app><rdg wit=\"collation_A\">is not easy</rdg><rdg wit=\"collation_B\">gets complicated very quickly</rdg><rdg wit=\"transcription\"><del>is not</del><add>gets</add> <del>easy</del><add>complicated</add> <add>very quickly</add></rdg></app> with long substitutions.</wit>", //
                    "<wit n=\"3\">It all gets very complicated with a lot of substitutions.</wit>"//
            ), asList(// een voorbeeld met een instant correction: geen <add> rond deel twee van de substitutie.
                    "<wit n=\"1\">It is not an easy thing at all with long substitutions.</wit>", //
                    "<wit n=\"2\">It <subst><del>is not easy at all</del> all gets complicated</subst> with long substitutions.</wit>", //
                    "<wit n=\"3\">It all gets very complicated with a lot of substitutions.</wit>"//
            ), asList(// een tweede optie van de instant correction: vooraf in subversies. Maar wat is dan het verschil met een gewone substitutie?
                    "<wit n=\"1\">It is not an easy thing at all with long substitutions.</wit>", //
                    "<wit n=\"2\">It <app><rdg wit=\"collation_A\">is not easy at all</rdg><rdg wit=\"collation_B\">all gets complicated</rdg><rdg wit=\"transcription\"><del>is not easy at all</del> all gets complicated</rdg></app> with long substitutions.</wit>", //
                    "<wit n=\"3\">It all gets very complicated with a lot of substitutions.</wit>"//
            ) //
    );

    @Before
    public void before() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunsch(new EqualityTokenComparator()));
    }

    @Test
    public void exampleTest() {
        this.examples.forEach(this::testCollation);
    }

    private void testCollation(List<String> witnessSet) {
        System.out.println("=>witnesses:");
        witnessSet.forEach(System.out::println);
        List<String> normalizedWitnesses = witnessSet.stream().map(this::stripXMLTags).collect(toList());// forEach(LOG::info);
        System.out.println("\n=>stripped:");
        normalizedWitnesses.forEach(System.out::println);
        String[] array = normalizedWitnesses.toArray(new String[normalizedWitnesses.size()]);
        System.out.println("\n=>collation:");
        VariantGraph variantGraph = collate(array);
        System.out.println(toString(table(variantGraph)));
        VariantGraphVisualizer variantGraphVisualizer = new VariantGraphVisualizer(variantGraph);
        System.out.println();
        System.out.println(variantGraphVisualizer.toASCII());
        System.out.println();
        System.out.println(variantGraphVisualizer.toHTML());
        System.out.println();
        System.out.println("================================================================================");
    }

    private String stripXMLTags(String xml) {
        return xml.replaceAll("<.*?>", " ")//
                .replaceAll(" +", " ")//
                .trim();
    }

    static class VariantGraphVisualizer {
        private VariantGraph variantGraph;

        public VariantGraphVisualizer(VariantGraph variantGraph) {
            this.variantGraph = variantGraph;
        }

        public String toASCII() {
            return ASCII.render(variantGraph);
        }

        public String toHTML() {
            return HTML.render(variantGraph);
        }

        static class ASCII {
            public static String render(VariantGraph variantGraph) {
                V2_AsciiTable table = new V2_AsciiTable();
                table.addStrongRule();
                variantGraph.witnesses().forEach(w -> addRow(w, table, variantGraph));
                return printTable(table);
            }

            private static void addRow(Witness witness, V2_AsciiTable table, VariantGraph variantGraph) {
                List<Object> row = StreamSupport.stream(variantGraph.vertices().spliterator(), false)//
                        .filter(vertex -> !vertex.tokens().isEmpty())//
                        .map(vertex -> toCell(vertex, witness))//
                        .collect(toList());
                addRow(table, row, 'c');
            }

            private static String toCell(Vertex vertex, Witness witness) {
                return vertex.tokens().stream()//
                        .filter(t -> t.getWitness().equals(witness))//
                        .map(SimpleToken.class::cast)//
                        .map(SimpleToken::getContent)//
                        .collect(joining(" "));
            }

            private static void addRow(V2_AsciiTable at, List<Object> row, char alignment) {
                at.addRow(row.toArray()).setAlignment(alignment(row, alignment));
                at.addRule();
            }

            private static char[] alignment(List<Object> row, char alignmentType) {
                char[] a = new char[row.size()];
                Arrays.fill(a, alignmentType);
                return a;
            }

            private static String printTable(V2_AsciiTable table) {
                return new V2_AsciiTableRenderer()//
                        .setTheme(V2_E_TableThemes.PLAIN_7BIT.get())//
                        .setWidth(new WidthLongestWord())//
                        .render(table)//
                        .toString();
            }
        }

        static class HTML {
            public static String render(VariantGraph variantGraph) {
                StringBuilder html = new StringBuilder("<table>\n");
                variantGraph.witnesses().forEach(w -> addRow(html, variantGraph, w));
                html.append("</table>");
                return html.toString();
            }

            private static void addRow(StringBuilder html, VariantGraph variantGraph, Witness w) {
                html.append("<tr>");
                StreamSupport.stream(variantGraph.vertices().spliterator(), false)//
                        .filter(vertex -> !vertex.tokens().isEmpty())//
                        .forEach(v -> addCell(html, v, w));
                // variantGraph.stream()//
                // .map(r -> r.getOrDefault(w, Collections.emptySet()))//
                // .map(this::toHTMLCell)//
                // .map(cell -> cell.isEmpty() ? "" : cell)//
                // .forEach(cell -> html.append("<td>").append(cell).append("</td>"));
                html.append("</tr>\n");
            }

            private static void addCell(StringBuilder html, Vertex vertex, Witness witness) {
                String cell = vertex.tokens().stream()//
                        .filter(t -> t.getWitness().equals(witness))//
                        .map(SimpleToken.class::cast)//
                        .map(SimpleToken::getContent)//
                        .collect(joining(" "));
                html.append("<td>").append(cell).append("</td>");
            }
        }

    }
}
