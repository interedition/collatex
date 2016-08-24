package eu.interedition.collatex.subst;

import static java.util.stream.Collectors.toList;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import de.vandermeer.asciitable.v2.RenderedTable;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthLongestWord;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

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
        LOG.info("\n" + xml_a + "\n" + xml_b + "\n" + xml_c + "\n");
        LayeredWitness witnessA = new LayeredWitness("1", xml_a);
        LayeredWitness witnessB = new LayeredWitness("2", xml_b);
        LayeredWitness witnessC = new LayeredWitness("3", xml_c);
        final VariantGraph graph = new VariantGraph();
        collationAlgorithm.collate(graph, witnessA, witnessB, witnessC);
        LOG.info(visualizeVariantGraphTable(table(graph)));
        // LOG.info(toString(table(graph)));
    }

    private String visualizeVariantGraphTable(List<SortedMap<Witness, Set<Token>>> list) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();

        witnesses(list)//
                .sorted(Witness.SIGIL_COMPARATOR)//
                .map(LayeredWitness.class::cast)//
                .forEach(witness -> {
                    List<Object> cells = list.stream()//
                            .map(r -> r.getOrDefault(witness, Collections.emptySet()))//
                            .map(tokens -> tokens.stream()//
                                    .filter(t -> LayerToken.class.isAssignableFrom(t.getClass()))//
                                    .map(LayerToken.class::cast)//
                                    .sorted()//
                                    .map(NeedlemanWunschForLayeredWitnessesAlgorithmTest::visualizeLayerToken)//
                                    .collect(Collectors.joining(" ")))//
                            .map(cell -> cell.isEmpty() ? " " : cell)//
                            .collect(toList());
                    addRow(table, cells, 'c');
                });

        RenderedTable rt = new V2_AsciiTableRenderer()//
                .setTheme(V2_E_TableThemes.UTF_LIGHT.get())//
                .setWidth(new WidthLongestWord())//
                .render(table);
        return "\n" + rt.toString();
    }

    protected static String toString(List<SortedMap<Witness, Set<Token>>> table) {
        return witnesses(table)//
                .sorted(Witness.SIGIL_COMPARATOR)//
                .map(witness -> String.format("%s: %s\n", witness.getSigil(), toString(table, witness)))//
                .collect(Collectors.joining());
    }

    protected static String toString(List<SortedMap<Witness, Set<Token>>> table, Witness witness) {
        return String.format("|%s|",
                table.stream()//
                        .map(r -> r.getOrDefault(witness, Collections.emptySet()))//
                        .map(tokens -> tokens.stream()//
                                .filter(t -> LayerToken.class.isAssignableFrom(t.getClass()))//
                                .map(LayerToken.class::cast)//
                                .sorted()//
                                .map(NeedlemanWunschForLayeredWitnessesAlgorithmTest::visualizeLayerToken)//
                                .collect(Collectors.joining(" "))//
                        )//
                        .map(cell -> cell.isEmpty() ? " " : cell)//
                        .collect(Collectors.joining("|")));
    }

    private static String visualizeLayerToken(LayerToken token) {
        String border = "";
        switch (token.getLayer()) {
        case "add":
            border = "+";
            break;
        case "del":
            border = "-";
            break;
        default:
            break;
        }
        return border + token.getContent().replace(" ", "\u2022") + border;
    }

    private void addRow(V2_AsciiTable at, List<Object> row, char alignment) {
        char[] alignmentArray = alignment(row, alignment);
        Object[] array = row.toArray();
        at.addRow(array)//
                .setAlignment(alignmentArray);
        at.addRule();
    }

    private char[] alignment(List<Object> row, char alignmentType) {
        char[] a = new char[row.size()];
        Arrays.fill(a, alignmentType);
        return a;
    }

}
