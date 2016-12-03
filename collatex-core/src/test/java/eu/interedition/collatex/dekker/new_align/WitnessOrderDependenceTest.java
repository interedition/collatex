package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ronald Dekker on 03/12/16.
 */
public class WitnessOrderDependenceTest extends AbstractTest {

    @Test
    public void testDifficultCase() {
//        1: a, b, c, d, e
//        2: a, e, c, d
//        3: a, d, b

        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
        AnotherAttemptAtAnAligner aligner = new AnotherAttemptAtAnAligner();
        VariantGraph vg = new VariantGraph();
        aligner.collate(vg, w);
        DecisionTree tree = aligner.decisionTree;
        DecisionNode root = tree.getRoot();
        DecisionNode node1 = root.getDecisionNodeChildForGraphPhrase();
        Match match1 = node1.getSelected();
        assertEquals("{[A:0:'a']; B:0:'a'}", match1.toString());
        DecisionNode node2 = node1.getDecisionNodeChildForGraphPhrase();
        Match match2 = node2.getSelected();
        assertEquals("{[A:2:'c']; B:2:'c'}", match2.toString());

    }

}
