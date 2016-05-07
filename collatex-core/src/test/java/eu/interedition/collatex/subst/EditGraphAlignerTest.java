package eu.interedition.collatex.subst;

import org.junit.Test;

import java.util.List;

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

        WitnessTree.WitnessNode wit_a = WitnessTree.createTree(xml_in);
        List<EditGraphAligner.EditGraphTableLabel> labels = EditGraphAligner.createLabels(wit_a);

        labels.forEach(label -> System.out.println(label));


    }
}
