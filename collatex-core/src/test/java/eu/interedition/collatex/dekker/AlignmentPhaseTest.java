package eu.interedition.collatex.dekker;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.new_align.AlignmentPhase;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by ronalddekker on 24/10/15.
 */
public class AlignmentPhaseTest extends AbstractTest {

    @Test
    public void testRepetition() {
        final SimpleWitness[] w = createWitnesses("the black cat on the table", "the black saw the black cat on the table", "the black saw\tthe black cat on the table");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        //aligner.collate(graph, w);
        // complicated setup to assert the data we want
        aligner.createTokenIndex(w);
        // align f1
        aligner.alignNextWitnessAndGraph(graph, true, w[0]);
        // align f2
        AlignmentPhase phase = aligner.alignNextWitnessAndGraph(graph, false, w[1]);
        assertEquals("[the, the black, the black, the black cat on the table, the, the, black, black cat on the table, black, cat on the table, on the table, the, the, the table, the, table]", phase.phraseMatchesGraphOrder.toString());
        assertEquals("[the, the black, the, black, the black, the black cat on the table, the, the, black cat on the table, black, cat on the table, on the table, the, the table, the, table]", phase.phraseMatchesWitnessOrder.toString());
    }


//    assertThat(graph, graph(w[0]).aligned("the").non_aligned("quick").aligned("brown", "fox", "jumps", "over", "the").non_aligned("lazy").aligned("dog"));
//    assertThat(graph, graph(w[1]).aligned("the").non_aligned("fast").aligned("brown", "fox", "jumps", "over", "the").non_aligned("black").aligned("dog"));
//    assertThat(graph, graph(w[2]).aligned("the").non_aligned("red").aligned("fox", "jumps", "over", "the").non_aligned("fence"));

}
