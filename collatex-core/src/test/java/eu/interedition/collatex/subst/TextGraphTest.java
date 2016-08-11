package eu.interedition.collatex.subst;

import static java.util.stream.Collectors.toList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.vandermeer.asciitable.v2.RenderedTable;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthLongestWord;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

public class TextGraphTest {
    Logger LOG = Logger.getLogger(this.getClass().getName());
    private static Driver neo4j;

    @BeforeClass
    public static void beforeClass() {
        neo4j = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "NEO4J"));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        neo4j.close();
    }

    @Test
    public void testexample1() {
        String xml_a = "<wit n=\"1\"><subst><del>Apparently, in</del><add>So, at</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\"><subst><del>Apparently, at</del><add>So, in</add></subst> <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";
        // String atRowA = "|Apparently, |in So, at |the | |beginning| outset |, finding the |correct |right word.|";
        String atRowA = "|Apparently:, |in:So:, :at|the :beginning|outset|, :finding :the |correct|right word.";
        String atRowB = "|Apparently:, |at:So:, :in|the|this:very |beginning:, :finding :the :right :word:.| |";
        // String atRowB = "|Apparently, |at So, in |the |this very |beginning| |, finding the | |right word.|";
        collate(xml_a, xml_b, atRowA, atRowB);
    }

    @Test
    public void testExample5() {
        String xml_a = "<wit n=\"1\">It is not an easy thing at all with long substitutions.</wit>";
        String xml_b = "<wit n=\"2\">It <subst><del>is not easy at all</del><add>all gets complicated</add></subst> with long substitutions.</wit>";
        String atRowA = "|Apparently, |in So, at |the | |beginning| outset |, finding the |correct |right word.|";
        String atRowB = "|Apparently, |at So, in |the |this very |beginning| |, finding the | |right word.|";
        collate(xml_a, xml_b, atRowA, atRowB);
    }

    @Test
    public void testExample6() {
        String xml_a = "<wit n=\"1\">It is not an easy thing at all with long substitutions.</wit>";
        String xml_b = "<wit n=\"2\">It <subst><del>is not</del><add>gets</add></subst> <subst><del>easy</del><add>complicated</add></subst> <subst><add>very quickly</add></subst> with long substitutions.</wit>";
        String atRowA = "|Apparently, |in So, at |the | |beginning| outset |, finding the |correct |right word.|";
        String atRowB = "|Apparently, |at So, in |the |this very |beginning| |, finding the | |right word.|";
        collate(xml_a, xml_b, atRowA, atRowB);
    }

    private void collate(String xml_a, String xml_b, String atRowA, String atRowB) {
        clearGraph();

        CollationGraph cg = new CollationGraph("testX", neo4j);
        Witness wA = cg.addWitness("A", xml_a);
        Witness wB = cg.addWitness("B", xml_b);
        cg.collate();
        List<SortedMap<Witness, Set<Token>>> table = cg.asTable();
        // cg.foldMatches();
        // visualizeAlignmentTable(table, wA, wB);
        System.out.println("<pre>");
        System.out.println(toHTML(xml_a));
        System.out.println(toHTML(xml_b));
        System.out.println("</pre>");
        System.out.println(toHTMLTable(table, cg.getWitnesses()));
        System.out.println("<hr/>");
        // assertEquals(atRowA, toString(table, wA));
        // assertEquals(atRowB, toString(table, wB));
    }


    private String toHTML(String xml) {
        return StringEscapeUtils.escapeHtml4(xml);
    }

    private void clearGraph() {
        try (Session session = neo4j.session()) {
            try (Transaction tx = session.beginTransaction()) {
                // remove all relations/edges
                tx.run("match (n)-[r]-() delete r");
                // remove all nodes/vertices
                tx.run("match (n) delete n");
                tx.success();
            }
        }
    }

    protected static String toString(List<SortedMap<Witness, Set<Token>>> table, Witness witness) {
        return String.format("|%s|",
                table.stream()//
                        .map(r -> r.getOrDefault(witness, Collections.emptySet()))//
                        .map(tokens -> tokens.stream()//
                                .filter(t -> LayerToken.class.isAssignableFrom(t.getClass()))//
                                .map(t -> (LayerToken) t)//
                                .sorted()//
                                .map(LayerToken::getContent)//
                                .map(t -> t.replace(" ", "\u2022"))//
                                .collect(Collectors.joining(":")))
                        .map(cell -> cell.isEmpty() ? " " : cell)//
                        .collect(Collectors.joining("|")));
    }

    private String toHTMLTable(List<SortedMap<Witness, Set<Token>>> alignmentTable, List<Witness> witnesses) {
        StringBuilder html = new StringBuilder("<table>\n");
        witnesses.stream().forEach(w -> {
            html.append("<tr>");
            alignmentTable.stream()//
                    .map(r -> r.getOrDefault(w, Collections.emptySet()))//
                    .map(this::toHTMLCell)//
                    .map(cell -> cell.isEmpty() ? "" : cell)//
                    .forEach(cell -> html.append("<td>").append(cell).append("</td>"));
            html.append("</tr>\n");
        });
        html.append("</table>");
        return html.toString();
    }

    private String toHTMLCell(Set<Token> tokens) {
        return tokens.stream()//
                .map(t -> (LayerToken) t)//
                .sorted()//
                .map(this::tokenAsHTML)//
                .collect(Collectors.joining());
    }

    private String tokenAsHTML(LayerToken t) {
        switch (t.getLayer()) {
        case "del":
            return "<del>" + t.getContent() + "</del>";

        case "add":
            return "<sup>" + t.getContent() + "</sup>";

        }
        return t.getContent();
    }

    private void visualizeAlignmentTable(List<SortedMap<Witness, Set<Token>>> alignmentTable, Witness witnessA, Witness witnessB) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addStrongRule();
        addRow(alignmentTable, witnessA, table);
        addRow(alignmentTable, witnessB, table);

        printTable(table);
    }

    private void addRow(List<SortedMap<Witness, Set<Token>>> alignmentTable, Witness witnessA, V2_AsciiTable table) {
        List<Object> row = alignmentTable.stream()//
                .map(r -> r.getOrDefault(witnessA, Collections.emptySet()))//
                .map(this::toCell)//
                .map(cell -> cell.isEmpty() ? " " : cell)//
                .collect(toList());
        addRow(table, row, 'c');
    }

    private String toCell(Set<Token> tokens) {
        return tokens.stream()//
                .map(t -> (LayerToken) t)//
                .sorted()//
                .map(t -> t.getContent().replace(" ", "\u2022") + "^" + t.getLayer().substring(0, 1))//
                .collect(Collectors.joining(":"));
    }

    private void addRow(V2_AsciiTable at, List<Object> row, char alignment) {
        at.addRow(row.toArray()).setAlignment(alignment(row, alignment));
        at.addRule();
    }

    private char[] alignment(List<Object> row, char alignmentType) {
        char[] a = new char[row.size()];
        Arrays.fill(a, alignmentType);
        return a;
    }

    private void printTable(V2_AsciiTable table) {
        RenderedTable rt = new V2_AsciiTableRenderer()//
                .setTheme(V2_E_TableThemes.UTF_LIGHT.get())//
                .setWidth(new WidthLongestWord())//
                .render(table);
        System.out.println(rt);
    }

}
