package eu.interedition.collatex.subst;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by ronalddekker on 15/08/16.
 */
public class IntegrationTest extends AbstractAlignmentTest {

//    Wit1:	The 	<subst>
//    <del hand=”#AA”>white</del>
//    <add hand=”#AA”>black</add>
//    </subst> dog.
//        Wit2:	The black dog.
//        Wit3: 	The white dog.
//        Wit4:	The blue dog.


    @Test
    public void testTwoWitnesses() {
        // Can we derive the correct XML output from a simple example?
        // First we start with two witnesses
        String w1 = "<wit n=\"1\">The <subst><del hand=\"#AA\">white</del><add hand=\"#AA\">black</add></subst> dog.</wit>";
        String w2 = "<wit n=\"2\">The black dog.</wit>";
        WitnessNode a = WitnessNode.createTree("A", w1);
        WitnessNode b = WitnessNode.createTree("B", w2);
        EditGraphAligner aligner = new EditGraphAligner(a, b);
        List<List<WitnessNode>> superWitness = aligner.getSuperWitness();
        visualizeSuperWitness(superWitness);
        // looks ok
    }


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
        Map<WitnessNode, String> witnessNodeToWitnessLabel;
        witnessNodeToWitnessLabel = aligner.getWitnessLabelsForSuperwitness(superWitness);

        // assert witness labels
        List<String> witnessLabels = new ArrayList<>();
        superWitness.forEach(l -> l.forEach(n -> witnessLabels.add(witnessNodeToWitnessLabel.get(n))));
        System.out.println(witnessLabels);

        List<String> expectedWitnessLabels = Arrays.asList("A", "B", "A-subst-del", "A-subst-add", "B", "A", "B", "A", "B");
        assertEquals(expectedWitnessLabels, witnessLabels);

    }

    // We have assign a rank to each witness node in the superwitness.
    // This is a bit more complex in the case of layers, since each layer should be its own witness
    // We rank the individual items in the superwitness.
    @Test
    public void testTwoWitnessesRanks() {
        String w1 = "<wit n=\"1\">The <subst><del hand=\"#AA\">white</del><add hand=\"#AA\">black</add></subst> dog.</wit>";
        String w2 = "<wit n=\"2\">The black dog.</wit>";
        WitnessNode a = WitnessNode.createTree("A", w1);
        WitnessNode b = WitnessNode.createTree("B", w2);
        EditGraphAligner aligner = new EditGraphAligner(a, b);
        List<List<WitnessNode>> superWitness = aligner.getSuperWitness();
        visualizeSuperWitness(superWitness);

        Map<List<WitnessNode>, Integer> witnessNodeToRank = aligner.getRanksForSuperwitness(superWitness);
        List<Integer> expectedRanks = Arrays.asList(0, 1, 1, 2, 3);
        assertEquals(expectedRanks, convertWitnessNodeRankMapToList(witnessNodeToRank, superWitness));
    }

}
