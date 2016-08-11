package eu.interedition.collatex.subst;

import static eu.interedition.collatex.subst.EditGraphAligner.createLabels;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.vandermeer.asciitable.v2.RenderedTable;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthLongestWord;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.util.VariantGraphRanking;

/**
 * Created by ronalddekker on 01/05/16.
 */
public class EditGraphAlignerTest {

    // for the alignment to work with the different layers of the text
    // we have to treat the start of an add/del tag differently
    // as well as the end of a subst tag
    // we have to map the tree structure to a flat structure of "labels" on the axis of the edit graph table

    @Test
    public void testLabelsOneAxis() {
        String xml_in = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";

        WitnessNode wit_a = WitnessNode.createTree("A", xml_in);
        List<EditGraphTableLabel> labels = createLabels(wit_a);

        labels.forEach(System.out::println);

    }

    @Test
    public void testScoringSimple() {
        String xml_a = "<wit n=\"1\">a b</wit>";
        String xml_b = "<wit n=\"2\">a c</wit>";

        WitnessNode wit_a = WitnessNode.createTree("A", xml_a);
        WitnessNode wit_b = WitnessNode.createTree("B", xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

        visualizeScoringMatrix(aligner);
    }

    // convenience method to convert a row of the scoring table into an Array of integers so we can easily test them
    private void assertTableRow(EditGraphAligner aligner, int row, List<Integer> expected) {
        List<Integer> actual = Stream.of(aligner.cells[row]).map(score -> score.globalScore).collect(toList());
        if (actual.size() != expected.size()) {
            Assert.fail("Lists not of same size: expected: " + expected.size() + ", but was: " + actual.size());
        }
        assertListEquals(expected, actual, aligner);

    }

    private void assertListEquals(List<Integer> expected, List<Integer> actual, EditGraphAligner aligner) {
        try {
            IntStream.range(0, expected.size()).forEach(index -> {
                assertEquals("Score at " + index + " differs: ", expected.get(index), actual.get(index));
            });
        } catch (AssertionError e) {
            visualizeScoringMatrix(aligner);
            throw e;
        }
    }

    @Test
    public void testScoringSubstSimpleVertical() {
        String xml_a = "<wit n=\"1\">a</wit>";
        String xml_b = "<wit n=\"2\"><subst><add>a</add><del>b</del><add>c</add></subst></wit>";

        WitnessNode wit_a = WitnessNode.createTree("A", xml_a);
        WitnessNode wit_b = WitnessNode.createTree("B", xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

        assertTableRow(aligner, 0, Arrays.asList(0, -1));
        assertTableRow(aligner, 1, Arrays.asList(-1, 0));
        assertTableRow(aligner, 2, Arrays.asList(-1, -2));
        assertTableRow(aligner, 3, Arrays.asList(-1, 0));
    }

    @Test
    public void testScoringSubstSimpleHorizontal() {
        String xml_a = "<wit n=\"1\"><subst><add>a</add><del>b</del><add>c</add></subst></wit>";
        String xml_b = "<wit n=\"2\">a</wit>";

        WitnessNode wit_a = WitnessNode.createTree("A", xml_a);
        WitnessNode wit_b = WitnessNode.createTree("B", xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

        assertTableRow(aligner, 0, Arrays.asList(0, -1, -1, -1));
        assertTableRow(aligner, 1, Arrays.asList(-1, 0, -2, 0));
    }

    private void debugScoringTable0(EditGraphAligner aligner) {
        IntStream.range(0, aligner.labelsWitnessB.size() + 1).forEach(y -> {
            IntStream.range(0, aligner.labelsWitnessA.size() + 1).forEach(x -> {
                System.out.printf("%3d | ", aligner.cells[y][x].globalScore);
            });
            System.out.println();
        });
    }

    // @Ignore
    @Test
    public void testScoring() {
        String xml_a = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\">In <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";

        visualizeAlignment(xml_a, xml_b);
    }

    @Test
    public void testScoring2() {
        String xml_a = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\"><subst><del>Since</del><add>From</add></subst> <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";

        visualizeAlignment(xml_a, xml_b);
    }

    @Test
    public void testScoring3() {
        String xml_a = "<wit n=\"1\"><subst><del>Apparently, in</del><add>So, at</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\"><subst><del>Apparently, at</del><add>So, in</add></subst> <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";

        visualizeAlignment(xml_a, xml_b);
    }

    @Test
    public void testAlign_1_2() {
        String xml_a = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>arguably correct</del><add>indubitably right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\">In the very beginning, finding the right word.</wit>";

        WitnessNode wit_a = WitnessNode.createTree("A", xml_a);
        WitnessNode wit_b = WitnessNode.createTree("B", xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

        visualizeScoringMatrix(aligner);

        List<Integer> scores = aligner.getBacktrackScoreStream()//
                .map(s -> s.globalScore).collect(toList());
        List<Integer> expected = Arrays.asList(-2, -2, -2, -2, -1, -1, -1, -1, -1, 0, 0);
        assertThat(scores).isEqualTo(expected);

        List<List<WitnessNode>> superWitness = aligner.getSuperWitness();
        visualizeSuperWitness(superWitness);
        List<String> expectedSuperWitness = ImmutableList.<String> of(//
                "A:In|B:In ", //
                "A:At", //
                "A:the |B:the ", //
                "B:very ", //
                "A:beginning|B:beginning", //
                "A:outset", //
                "A:, |B:, ", //
                "A:finding |B:finding ", //
                "A:the |B:the ", //
                "A:arguably ", //
                "A:correct", //
                "A:indubitably ", //
                "A:right|B:right ", //
                "A:word|B:word", //
                "A:.|B:."//
        );
        List<String> assertableRepresentation = serialize(superWitness);
        assertThat(assertableRepresentation).containsExactlyElementsOf(expectedSuperWitness);

    }

    @Test
    public void testAlign_2_1() throws Exception {
        String xml_a = "<wit n=\"2\">But in the very beginning, finding the right word.</wit>";
        String xml_b = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>arguably correct</del><add>indubitably right</add></subst> word.</wit>";
        List<String> expectedSuperWitness = ImmutableList.<String> of(//
                "A:But ", //
                "A:in |B:In", //
                "B:At", //
                "A:the |B:the ", //
                "A:very ", //
                "A:beginning|B:beginning", //
                "B:outset", //
                "A:, |B:, ", //
                "A:finding |B:finding ", //
                "A:the |B:the ", //
                "B:arguably ", //
                "B:correct", //
                "B:indubitably ", //
                "A:right |B:right", //
                "A:word|B:word", //
                "A:.|B:."//
        );

        visualizeAlignment(xml_a, xml_b, expectedSuperWitness);
    }

    @Test
    public void testAlign_2_3() throws Exception {
        String xml_a = "<wit n=\"wit2\">In <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";
        String xml_b = "<wit n=\"wit3\">At the outset, looking for the best word.</wit>";
        List<String> expectedSuperWitness = ImmutableList.<String> of(//
                "A:In ", //
                "B:At ", //
                "A:the|B:the ", //
                "A:this", //
                "A:very ", //
                "A:beginning", //
                "B:outset", //
                "A:, |B:, ", //
                "A:finding ", //
                "B:looking ", //
                "B:for ", //
                "A:the |B:the ", //
                "A:right ", //
                "B:best ", //
                "A:word|B:word", //
                "A:.|B:."//
        );

        visualizeAlignment(xml_a, xml_b, expectedSuperWitness);
    }

    private List<List<WitnessNode>> visualizeAlignment(String xml_a, String xml_b) {
        WitnessNode wit_a = WitnessNode.createTree("A", xml_a);
        WitnessNode wit_b = WitnessNode.createTree("B", xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);
        visualizeScoringMatrix(aligner);

        List<Integer> scores = aligner.getBacktrackScoreStream().map(s -> s.globalScore).collect(toList());
        System.out.println("best path scores = " + scores);

        List<List<WitnessNode>> superWitness = aligner.getSuperWitness();
        System.out.println("superwitness = " + serialize(superWitness));
        visualizeSuperWitness(superWitness);
        return superWitness;
    }

    private void visualizeAlignment(String xml_a, String xml_b, List<String> expectedSuperWitness) {
        List<List<WitnessNode>> superWitness = visualizeAlignment(xml_a, xml_b);
        List<String> assertableRepresentation = serialize(superWitness);
        assertThat(assertableRepresentation).containsExactlyElementsOf(expectedSuperWitness);
    }

    private List<String> serialize(List<List<WitnessNode>> superWitness) {
        return superWitness.stream()//
                .map(this::serializeWitnessNodeList)//
                .collect(toList());
    }

    private String serializeWitnessNodeList(List<WitnessNode> list) {
        return list.stream()//
                .map(this::serializeWitnessNode)//
                .collect(joining("|"));
    }

    private String serializeWitnessNode(WitnessNode wn) {
        return wn.getSigil() + ":" + wn.data;
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

    private String labelText(EditGraphTableLabel l) {
        return witnessNodeText(l.text);
    }

    private String witnessNodeText(WitnessNode witnessNode) {
        return witnessNode.data.replace(" ", "\u2022");
    }

    static final Map<Score.Type, String> TYPEINDICATORS = ImmutableMap.<Score.Type, String> builder()//
            .put(Score.Type.match, "!")//
            .put(Score.Type.mismatch, "%")//
            .put(Score.Type.addition, "+")//
            .put(Score.Type.deletion, "-")//
            .build();

    private void visualizeScoringMatrix(EditGraphAligner aligner) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addStrongRule();
        List<Object> row = aligner.labelsWitnessA.stream().map(this::labelText).collect(toList());
        row.add(0, "");
        row.add(0, "");
        addRow(table, row, 'c');
        for (int y = 0; y < aligner.labelsWitnessB.size() + 1; y++) {
            row.clear();
            row.add(y == 0 ? "" : labelText(aligner.labelsWitnessB.get(y - 1)));
            for (int x = 0; x < aligner.labelsWitnessA.size() + 1; x++) {
                Score score = aligner.cells[y][x];
                Object cell = score.globalScore;
                if (TYPEINDICATORS.containsKey(score.type)) {
                    cell = cell + TYPEINDICATORS.get(score.type);
                }
                row.add(cell);
            }
            addRow(table, row, 'r');
        }

        printTable(table);
    }

    private void printTable(V2_AsciiTable table) {
        RenderedTable rt = new V2_AsciiTableRenderer()//
                .setTheme(V2_E_TableThemes.UTF_LIGHT.get())//
                .setWidth(new WidthLongestWord())//
                .render(table);
        System.out.println(rt);
    }

    private void visualizeSuperWitness(List<List<WitnessNode>> superWitness) {
        List<Object> witATokens = Lists.newArrayList("A:");
        List<Object> witABTokens = Lists.newArrayList("A+B:");
        List<Object> witBTokens = Lists.newArrayList("B:");

        superWitness.forEach(l -> {
            // System.err.println("l=" + l);
            WitnessNode witnessNode = l.get(0);
            String witnessNodeText = witnessNodeText(witnessNode);
            if (l.size() == 2) {
                witATokens.add("");
                witABTokens.add(witnessNodeText);
                witBTokens.add("");

            } else if (l.size() == 1) {
                witABTokens.add("");
                if ("A".equals(witnessNode.getSigil())) {
                    witATokens.add(witnessNodeText);
                    witBTokens.add("");
                } else if ("B".equals(witnessNode.getSigil())) {
                    witATokens.add("");
                    witBTokens.add(witnessNodeText);
                }

            }
        });

        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        addRow(table, witATokens, 'c');
        addRow(table, witABTokens, 'c');
        addRow(table, witBTokens, 'c');
        printTable(table);

    }

    private void visualizeVariantGraph(VariantGraph vGraph) {
        List<Object> witATokens = Lists.newArrayList();
        List<Object> witABTokens = Lists.newArrayList();
        List<Object> witBTokens = Lists.newArrayList();

        List<String> allSigils = vGraph.witnesses().stream().map(Witness::getSigil).sorted().collect(toList());
        Preconditions.checkState(allSigils.size() == 2, "Currently only works for VariantGraphs with 2 witnesses, sorry");
        String sigilA = allSigils.get(0);
        String sigilB = allSigils.get(1);
        String sigilAB = sigilA + "+" + sigilB;

        final Iterator<Set<Vertex>> rankedIterator = VariantGraphRanking.of(vGraph).iterator();
        while (rankedIterator.hasNext()) {
            Map<String, Object> grouped = Maps.newLinkedHashMap();
            grouped.put(sigilA, "");
            grouped.put(sigilAB, "");
            grouped.put(sigilB, "");
            for (Vertex vertex : rankedIterator.next()) {
                String sigils = vertex.witnesses().stream().map(Witness::getSigil).sorted().collect(joining("+"));
                grouped.put(sigils, vertex.tokens().stream().map(Token::toString).collect(joining("")));
            }
            witATokens.add(grouped.get(sigilA));
            witABTokens.add(grouped.get(sigilAB));
            witBTokens.add(grouped.get(sigilB));
        }

        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        addRow(table, witATokens, 'c');
        addRow(table, witABTokens, 'c');
        addRow(table, witBTokens, 'c');
        printTable(table);
    }

}
