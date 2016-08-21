package eu.interedition.collatex.subst;

import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ronalddekker on 15/08/16.
 */
public class XMLOutputTest extends AbstractAlignmentTest {

    // Can we derive the correct XML output from a simple example?
    // First we start with two witnesses
    // we have assign a witness to each witness node in the superwitness
    // this is a bit more complex in the case of layers, since each layer should be its own witness
    @Test
    public void testTwoWitnessesLayerIdentifiers() {
        String w1 = "<wit n=\"1\">The <subst><del hand=\"#AA\">white</del><add hand=\"#AA\">black</add></subst> dog.</wit>";
        String w2 = "<wit n=\"2\">The black dog.</wit>";
        WitnessNode a = WitnessNode.createTree("A", w1);
        WitnessNode b = WitnessNode.createTree("B", w2);
        EditGraphAligner aligner = new EditGraphAligner(a, b);
        List<List<WitnessNode>> superWitness = aligner.getSuperWitness();
        visualizeSuperWitness(superWitness);

        XMLOutput output = new XMLOutput(superWitness);
        Map<WitnessNode, String> witnessNodeToWitnessLabel;
        witnessNodeToWitnessLabel = output.getWitnessLabels();

        // assert witness labels
        List<String> witnessLabels = new ArrayList<>();
        superWitness.forEach(l -> l.forEach(n -> witnessLabels.add(witnessNodeToWitnessLabel.get(n))));
        System.out.println(witnessLabels);

        List<String> expectedWitnessLabels = Arrays.asList("A", "B", "A-subst-del", "A-subst-add", "B", "A", "B", "A", "B");
        assertEquals(expectedWitnessLabels, witnessLabels);

    }

// TODO: this test is a work in progress!
//            The
//            <app>
//        <rdg wit=”#Wit1-subst-del #Wit3”>white</rdg>
//        <rdg wit=”#Wit1-subst-add #Wit2”>black</rdg>
//        <rdg wit=”#Wit4”>blue</rdg>
//        </app>
//            dog.
    @Ignore
    @Test
    public void testXMLOutputTwoWitnesses() {
        String w1 = "<wit n=\"1\">The <subst><del hand=\"#AA\">white</del><add hand=\"#AA\">black</add></subst> dog.</wit>";
        String w2 = "<wit n=\"2\">The black dog.</wit>";
        WitnessNode a = WitnessNode.createTree("A", w1);
        WitnessNode b = WitnessNode.createTree("B", w2);
        EditGraphAligner aligner = new EditGraphAligner(a, b);
        List<List<WitnessNode>> superWitness = aligner.getSuperWitness();
        XMLOutput x = new XMLOutput(superWitness);
        List<XMLOutput.Column> table = x.getTable();
        Iterator<XMLOutput.Column> columns = table.iterator();
        XMLOutput.Column column1 = columns.next();
        assertFalse(column1.hasVariation());
        assertEquals("The ", column1.getReading());
        XMLOutput.Column column2 = columns.next();
        assertTrue(column2.hasVariation());
        assertEquals("white", column2.getReading("A-subst-del"));



    }

}
