package eu.interedition.collatex.subst;

import static eu.interedition.collatex.subst.EditGraphAligner.createLabels;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.vandermeer.asciitable.v2.RenderedTable;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthLongestWord;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.subst.EditGraphAligner.EditGraphTableLabel;
import eu.interedition.collatex.subst.EditGraphAligner.Score;

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

        WitnessNode wit_a = WitnessNode.createTree(xml_in);
        List<EditGraphAligner.EditGraphTableLabel> labels = createLabels(wit_a);

        labels.forEach(System.out::println);

    }

    @Test
    public void testScoringSimple() {
        String xml_a = "<wit n=\"1\">a b</wit>";
        String xml_b = "<wit n=\"2\">a c</wit>";

        WitnessNode wit_a = WitnessNode.createTree(xml_a);
        WitnessNode wit_b = WitnessNode.createTree(xml_b);

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

        WitnessNode wit_a = WitnessNode.createTree(xml_a);
        WitnessNode wit_b = WitnessNode.createTree(xml_b);

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

        WitnessNode wit_a = WitnessNode.createTree(xml_a);
        WitnessNode wit_b = WitnessNode.createTree(xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

        assertTableRow(aligner, 0, Arrays.asList(0, -1, -1, -1));
        assertTableRow(aligner, 1, Arrays.asList(-1, 0, -2, 0));

        Witness superW = toSuperWitness(aligner);
        assertNotNull(superW);
    }

    private Witness toSuperWitness(EditGraphAligner aligner) {
        SimpleWitness w = new SimpleWitness("S");
        List<Token> tokens = new ArrayList<>();
        w.setTokens(tokens);
        return w;
    }

    private void debugScoringTable0(EditGraphAligner aligner) {
        IntStream.range(0, aligner.labelsWitnessB.size() + 1).forEach(y -> {
            IntStream.range(0, aligner.labelsWitnessA.size() + 1).forEach(x -> {
                System.out.printf("%3d | ", aligner.cells[y][x].globalScore);
            });
            System.out.println();
        });
    }

    @Ignore
    @Test
    public void testScoring() {
        String xml_a = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\">In <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";

        WitnessNode wit_a = WitnessNode.createTree(xml_a);
        WitnessNode wit_b = WitnessNode.createTree(xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

        visualizeScoringMatrix(aligner);
    }

    @Test
    public void testBacktrackScoreStream() {
        String xml_a = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\">In the very beginning, finding the right word.</wit>";

        WitnessNode wit_a = WitnessNode.createTree(xml_a);
        WitnessNode wit_b = WitnessNode.createTree(xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

        visualizeScoringMatrix(aligner);

        Stream<EditGraphAligner.Score> backtrackScoresStream = aligner.getBacktrackScoreStream();
        List<Integer> scores = backtrackScoresStream.map(s -> s.globalScore).collect(toList());
        List<Integer> expected = Arrays.asList(-1, -1, -1, -2, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0);
        assertEquals(expected, scores);
    }

    private void visualizeScoringMatrix(EditGraphAligner aligner) {
        V2_AsciiTable at = new V2_AsciiTable();
        at.addStrongRule();
        List<Object> row = aligner.labelsWitnessA.stream().map(this::labelText).collect(toList());
        row.add(0, "");
        row.add(0, "");
        addRow(at, row, 'c');
        for (int y = 0; y < aligner.labelsWitnessB.size() + 1; y++) {
            row.clear();
            row.add(y == 0 ? "" : labelText(aligner.labelsWitnessB.get(y - 1)));
            for (int x = 0; x < aligner.labelsWitnessA.size() + 1; x++) {
                Score score = aligner.cells[y][x];
                Object cell = score.globalScore;
                if (score.isMatch()) {
                    cell = ">" + cell + "<";
                }
                row.add(cell);
            }
            addRow(at, row, 'r');
        }
        RenderedTable rt = new V2_AsciiTableRenderer()//
                .setTheme(V2_E_TableThemes.UTF_LIGHT.get())//
                .setWidth(new WidthLongestWord())//
                .render(at);
        System.out.println(rt);
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
        return l.text.data.replace(" ", "\u2022");
    }

}
