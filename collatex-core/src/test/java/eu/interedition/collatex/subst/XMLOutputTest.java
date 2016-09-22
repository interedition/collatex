package eu.interedition.collatex.subst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * Created by ronalddekker on 15/08/16.
 */
public class XMLOutputTest extends AbstractAlignmentTest {

    // Can we derive the correct XML output from a simple example?
    // First we start with two witnesses
    // we have assign a witness/layer label/identifier to each witness node in the superwitness
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

        List<String> expectedWitnessLabels = Arrays.asList("A", "B", "A", "A", "B", "A", "B", "A", "B");
        // List<String> expectedWitnessLabels = Arrays.asList("A", "B", "A-subst-del", "A-subst-add", "B", "A", "B", "A", "B");
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

        XMLOutput output = new XMLOutput(superWitness);
        Map<List<WitnessNode>, Integer> witnessNodeToRank = output.getRanksForMatchesAndNonMatches();
        List<Integer> expectedRanks = Arrays.asList(0, 1, 1, 2, 3);
        assertEquals(expectedRanks, convertWitnessNodeRankMapToList(witnessNodeToRank, superWitness));
    }

    // The
    // <app>
    // <rdg wit=”#WitA-subst-del”>white</rdg>
    // <rdg wit=”#WitA-subst-add #WitB”>black</rdg>
    // </app>
    // dog.

    @Test
    public void testColumnGenerationTwoWitnesses() {
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
        assertEquals("The ", column1.getLemma());
        XMLOutput.Column column2 = columns.next();
        assertTrue(column2.hasVariation());
        assertEquals(Arrays.asList("white", "black"), column2.getReadings());
        // assertEquals("A-subst-del", column2.getWitnessesForReading("white").get(0));
        // assertEquals(Arrays.asList("A-subst-add", "B"), column2.getWitnessesForReading("black"));
        XMLOutput.Column column3 = columns.next();
        assertFalse(column3.hasVariation());
        assertEquals("dog", column3.getLemma());
        XMLOutput.Column column4 = columns.next();
        assertFalse(column4.hasVariation());
        assertEquals(".", column4.getLemma());
        assertFalse(columns.hasNext());
    }

    // The
    // <app>
    // <rdg wit=”#WitA-subst-del”>white</rdg>
    // <rdg wit=”#WitA-subst-add #WitB”>black</rdg>
    // </app>
    // dog.

    // From the columns we go to the actual XML
    // TODO: whitespace in the dog token is still a problem, since it is preceding the token and not following it.
    @Test
    public void testGenerateXML() throws FileNotFoundException, UnsupportedEncodingException, XMLStreamException {
        String w1 = "<wit n=\"1\">The <subst><del hand=\"#AA\">white</del><add hand=\"#AA\">black</add></subst> dog.</wit>";
        String w2 = "<wit n=\"2\">The black dog.</wit>";
        WitnessNode a = WitnessNode.createTree("A", w1);
        WitnessNode b = WitnessNode.createTree("B", w2);
        EditGraphAligner aligner = new EditGraphAligner(a, b);
        List<List<WitnessNode>> superWitness = aligner.getSuperWitness();
        XMLOutput x = new XMLOutput(superWitness);
        StringWriter writer = new StringWriter();
        x.printXML(writer);
        String content = writer.toString();
        System.out.println(content);
        // disable until the final format is determined
        // assertEquals("<?xml version=\"1.0\" ?><apparatus>The <app><rdg wit=\"A-subst-del\">white</rdg><rdg wit=\"A-subst-add B\">black</rdg></app>dog.</apparatus>", content);
    }
}
