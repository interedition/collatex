package eu.interedition.collatex.subst;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

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
        List<EditGraphAligner.EditGraphTableLabel> labels = EditGraphAligner.createLabels(wit_a);

        labels.forEach(System.out::println);


    }

    @Test
    public void testScoringSimple() {
        String xml_a = "<wit n=\"1\">a b</wit>";
        String xml_b = "<wit n=\"2\">a c</wit>";

        WitnessNode wit_a = WitnessNode.createTree(xml_a);
        WitnessNode wit_b = WitnessNode.createTree(xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

        debugScoringTable(aligner);
    }


    // convenience method to convert a row of the scoring table into an Array of integers so we can easily test them
    private void assertTableRow(EditGraphAligner aligner, int row, List<Integer> expected) {
        List<Integer> actual = Stream.of(aligner.cells[row]).map(score -> score.globalScore).collect(toList());
        if (actual.size() != expected.size()) {
            Assert.fail("Lists not of same size: expected: "+expected.size()+", but was: "+actual.size());
        }
        assertListEquals(expected, actual, aligner);

    }

    private void assertListEquals(List<Integer> expected, List<Integer> actual, EditGraphAligner aligner) {
        try {
            IntStream.range(0, expected.size()).forEach( index -> {
                assertEquals("Score at "+index+" differs: ", expected.get(index), actual.get(index));
            });
        } catch (AssertionError e) {
            debugScoringTable(aligner);
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
    }



    private void debugScoringTable(EditGraphAligner aligner) {
        IntStream.range(0, aligner.labelsWitnessB.size() + 1).forEach(y -> {
            IntStream.range(0, aligner.labelsWitnessA.size() + 1).forEach(x -> {
                System.out.print(aligner.cells[y][x].globalScore + "|");
            });
            System.out.println();
        });
    }


    @Test
    public void testScoring() {
        String xml_a = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\">In <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";

        WitnessNode wit_a = WitnessNode.createTree(xml_a);
        WitnessNode wit_b = WitnessNode.createTree(xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

        debugScoringTable(aligner);
    }
}
