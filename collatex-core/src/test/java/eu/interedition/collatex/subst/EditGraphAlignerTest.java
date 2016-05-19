package eu.interedition.collatex.subst;

import org.junit.Test;

import java.util.List;
import java.util.stream.IntStream;

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

        IntStream.range(0, aligner.labelsWitnessB.size() + 1).forEach(y -> {
            IntStream.range(0, aligner.labelsWitnessA.size() + 1).forEach(x -> {
                System.out.print(aligner.cells[y][x].globalScore + "|");
            });
            System.out.println();
        });
    }

    @Test
    public void testScoringSubstSimple() {
        String xml_a = "<wit n=\"1\">a</wit>";
        String xml_b = "<wit n=\"2\"><subst><add>a</add><del>b</del><add>c</add></subst></wit>";

        WitnessNode wit_a = WitnessNode.createTree(xml_a);
        WitnessNode wit_b = WitnessNode.createTree(xml_b);

        EditGraphAligner aligner = new EditGraphAligner(wit_a, wit_b);

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

        IntStream.range(0, aligner.labelsWitnessB.size() + 1).forEach(y -> {
            IntStream.range(0, aligner.labelsWitnessA.size() + 1).forEach(x -> {
                System.out.print(aligner.cells[y][x].globalScore + "|");
            });
            System.out.println();
        });
    }
}
