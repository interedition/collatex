package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ronalddekker on 24/10/15.
 */
public class DecisionTreeBuilderTest extends AbstractTest {

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
        DecisionTreeBuilder phase = aligner.alignNextWitnessAndGraph(graph, false, w[1]);
        assertEquals("[the black cat on the table, the black, the black, the, the, the, black cat on the table, black, black, cat on the table, on the table, the table, the, the, the, table]", phase.phraseMatchesGraphOrder.toString());
        assertEquals("[the black, the, the, black, the black cat on the table, the black, the, the, black cat on the table, black, cat on the table, on the table, the table, the, the, table]", phase.phraseMatchesWitnessOrder.toString());
    }

    @Test
    public void testTransposition() {
        SimpleWitness[] w = createWitnesses("This morning the cat observed little birds in the trees.",
            "The cat was observing birds in the little trees this morning, it observed birds for two hours.");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        // complicated setup to assert the data we want
        TokenIndex tokenIndex = aligner.createTokenIndex(w);
        // align f1 (this is just to fill the variant graph with the first witness)
        aligner.alignNextWitnessAndGraph(graph, true, w[0]);
        // align f2
        //DecisionTreeBuilder phase = aligner.alignNextWitnessAndGraph(graph, false, w[1]);
        DecisionTreeBuilder builder = new DecisionTreeBuilder();
        DecisionTree decisionTree = builder.create(tokenIndex, graph, w[1], aligner.vertexArray);
        DecisionNode root = decisionTree.getRoot();

        // 1 graph
        DecisionNode graphNode = root.getDecisionNodeChildForGraphPhrase();
        // assert selection
        assertEquals("this morning", graphNode.getSelected().get(0).toString());
        // assert moved (1)
        assertEquals("[the cat, birds in the, little, trees]", graphNode.getMoved().toString());
        // assert new position
        assertEquals("observed", graphNode.peekGraphPhrase().toString());
        assertEquals("observed", graphNode.peekWitnessPhrase().toString());

        // 1 witness
        DecisionNode witnessNode = root.getDecisionNodeChildForWitnessPhrase();
        // assert selection
        assertEquals("the cat", witnessNode.getSelected().get(0).toString());
        // assert moved (1)
        assertEquals("[this morning]", witnessNode.getMoved().toString());
        // assert new position
        assertEquals("observed", witnessNode.peekGraphPhrase().toString());
        assertEquals("birds in the", witnessNode.peekWitnessPhrase().toString());

        // 2
        DecisionNode witnessNode2 = witnessNode.getDecisionNodeChildForWitnessPhrase();
        // assert selection
        assertEquals("birds in the", witnessNode2.getSelected().get(0).toString());
        // assert moved (2)
        assertEquals("[observed, little]", witnessNode2.getMoved().toString());
        // assert new position
        assertEquals("trees", witnessNode2.peekGraphPhrase().toString());
        assertEquals("trees", witnessNode2.peekWitnessPhrase().toString());

        // 3
        DecisionNode witnessNode3 = witnessNode2.getDecisionNodeChildForWitnessPhrase();
        // assert selection
        assertEquals("trees", witnessNode3.getSelected().get(0).toString());
        // assert moved
        assertEquals("[]", witnessNode3.getMoved().toString());
        // assert new position
        assertEquals(".", witnessNode3.peekGraphPhrase().toString());
        assertEquals(".", witnessNode3.peekWitnessPhrase().toString());

        // 4
        DecisionNode witnessNode4 = witnessNode3.getDecisionNodeChildForWitnessPhrase();
        // assert selection
        assertEquals(".", witnessNode4.getSelected().get(0).toString());
        // assert moved
        assertEquals("[]", witnessNode4.getMoved().toString());
        assertTrue(witnessNode4.isGraphEnd());
        assertTrue(witnessNode4.isWitnessEnd());
    }




//    assertThat(graph, graph(w[0]).aligned("the").non_aligned("quick").aligned("brown", "fox", "jumps", "over", "the").non_aligned("lazy").aligned("dog"));
//    assertThat(graph, graph(w[1]).aligned("the").non_aligned("fast").aligned("brown", "fox", "jumps", "over", "the").non_aligned("black").aligned("dog"));
//    assertThat(graph, graph(w[2]).aligned("the").non_aligned("red").aligned("fox", "jumps", "over", "the").non_aligned("fence"));

}
