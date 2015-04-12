package eu.interedition.collatex.dekker;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static eu.interedition.collatex.dekker.VariantGraphMatcher.graph;
import static org.junit.Assert.*;

/**
 * Created by ronald on 3/31/15.
 */
public class dekker21GroupAlignerTest extends AbstractTest {

    private void assertLCPInterval(String blabla, LCP_Interval lcp_interval, Dekker21Aligner aligner) {
        assertEquals(blabla, aligner.getNormalizedForm(lcp_interval));
    }

    private void assertNode(int i, int j, Dekker21Aligner.ExtendedGraphNode decisionGraphNode) {
        assertEquals(i, decisionGraphNode.getVertexRank());
        assertEquals(j, decisionGraphNode.startPosWitness2);
    }


    @Test
    public void testCaseDecisionGraphFirstWitness() {
        final SimpleWitness[] w = createWitnesses("The same stuff", "The same stuff");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        VariantGraph g = new VariantGraph();
        // we collate the first witness --> is a simple add
        aligner.collate(g, w[0]);
        VariantGraph.Vertex[] vertices = aligner.vertex_array;
        assertVertexEquals("the", vertices[0]);
        assertVertexEquals("same", vertices[1]);
        assertVertexEquals("stuff", vertices[2]);
    }

    @Test
    public void testCaseDecisionGraphThreeWitnessesMatches() {
        final SimpleWitness[] w = createWitnesses("The same stuff", "The same stuff");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        VariantGraph g = new VariantGraph();
        // we collate the first witness --> is a simple add
        aligner.collate(g, w[0]);
        // then we examine the decision graph for the third witness
        Dekker21Aligner.ThreeDimensionalDecisionGraph decisionGraph = aligner.createDecisionGraph(g, w[1]);
        Dekker21Aligner.ExtendedGraphNode root = decisionGraph.getRoot();
        // create neighbor nodes
        decisionGraph.neighborNodes(root);

        // check the neighbors of the root node
        Dekker21Aligner.ExtendedGraphEdge edge1 = decisionGraph.edgeBetween(root, Dekker21Aligner.EditOperationEnum.SKIP_TOKEN_GRAPH);
        Dekker21Aligner.ExtendedGraphEdge edge2 = decisionGraph.edgeBetween(root, Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE);
        Dekker21Aligner.ExtendedGraphEdge edge3 = decisionGraph.edgeBetween(root, Dekker21Aligner.EditOperationEnum.SKIP_TOKEN_WITNESS);

        Dekker21Aligner.ExtendedGraphNode target1 = decisionGraph.getTarget(edge1);
        Dekker21Aligner.ExtendedGraphNode target2 = decisionGraph.getTarget(edge2);
        Dekker21Aligner.ExtendedGraphNode target3 = decisionGraph.getTarget(edge3);

        // assert LCP interval on the edges
        assertLCPInterval("the same stuff", edge1.lcp_interval, aligner);
        assertLCPInterval("the same stuff", edge2.lcp_interval, aligner);
        assertLCPInterval("the same stuff", edge3.lcp_interval, aligner);

        assertNode(3, 0, target1);
        assertNode(3, 3, target2);
        assertNode(0, 3, target3);

        // check goal node
        assertFalse(decisionGraph.isGoal(target1));
        assertTrue(decisionGraph.isGoal(target2));
        assertFalse(decisionGraph.isGoal(target3));

        // check costs of target nodes
        assertEquals(0, decisionGraph.distBetween(root, target1).alignedTokens);
        assertEquals(3, decisionGraph.distBetween(root, target2).alignedTokens);
        assertEquals(0, decisionGraph.distBetween(root, target3).alignedTokens);

        // check alignment path
        List<Dekker21Aligner.ExtendedGraphEdge> edges = decisionGraph.getOptimalPath();
        Dekker21Aligner.ExtendedGraphEdge edge = edges.get(0);
        assertEquals(Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE, edge.operation);
        assertLCPInterval("the same stuff", edge.lcp_interval, aligner);

        // check the actual alignment
        aligner.collate(g, w[1]);
        assertThat(g, graph(w[0]).aligned("the", "same", "stuff"));
        assertThat(g, graph(w[1]).aligned("the", "same", "stuff"));
    }

    @Test
    public void testCaseDecisionGraphTwoWitnessesReplacement() {
        final SimpleWitness[] w = createWitnesses("The black cat", "The red cat");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        VariantGraph g = new VariantGraph();
        // we collate the first witness --> is a simple add
        aligner.collate(g, w[0]);
        // then we examine the decision graph for the third witness
        Dekker21Aligner.ThreeDimensionalDecisionGraph decisionGraph = aligner.createDecisionGraph(g, w[1]);
        Dekker21Aligner.ExtendedGraphNode root = decisionGraph.getRoot();
        // create neighbor nodes
        decisionGraph.neighborNodes(root);

        // check the neighbors of the root node
        Dekker21Aligner.ExtendedGraphEdge edge1 = decisionGraph.edgeBetween(root, Dekker21Aligner.EditOperationEnum.SKIP_TOKEN_GRAPH);
        Dekker21Aligner.ExtendedGraphEdge edge2 = decisionGraph.edgeBetween(root, Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE);
        Dekker21Aligner.ExtendedGraphEdge edge3 = decisionGraph.edgeBetween(root, Dekker21Aligner.EditOperationEnum.SKIP_TOKEN_WITNESS);

        Dekker21Aligner.ExtendedGraphNode target1 = decisionGraph.getTarget(edge1);
        Dekker21Aligner.ExtendedGraphNode target2 = decisionGraph.getTarget(edge2);
        Dekker21Aligner.ExtendedGraphNode target3 = decisionGraph.getTarget(edge3);

        // assert LCP interval on the edges
        assertLCPInterval("the", edge1.lcp_interval, aligner);
        assertLCPInterval("the", edge2.lcp_interval, aligner);
        assertLCPInterval("the", edge3.lcp_interval, aligner);

        assertNode(1, 0, target1);
        assertNode(1, 1, target2);
        assertNode(0, 1, target3);

        // check goal node
        assertFalse(decisionGraph.isGoal(target1));
        assertFalse(decisionGraph.isGoal(target2));
        assertFalse(decisionGraph.isGoal(target3));

        // check costs of target nodes
        assertEquals(0, decisionGraph.distBetween(root, target1).alignedTokens);
        assertEquals(1, decisionGraph.distBetween(root, target2).alignedTokens);
        assertEquals(0, decisionGraph.distBetween(root, target3).alignedTokens);

        // check heuristic costs of target nodes
        assertEquals(1, decisionGraph.heuristicCostEstimate(target1).alignedTokens);
        assertEquals(1, decisionGraph.heuristicCostEstimate(target2).alignedTokens);
        assertEquals(1, decisionGraph.heuristicCostEstimate(target3).alignedTokens);

        //TODO: check alignment path!

//        // check alignment path
//        List<Dekker21Aligner.ExtendedGraphEdge> edges = decisionGraph.getOptimalPath();
//        Dekker21Aligner.ExtendedGraphEdge edge = edges.get(0);
//        assertEquals(Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE, edge.operation);
//        assertLCPInterval("the same stuff", edge.lcp_interval, aligner);
//
//        // check the actual alignment
//        aligner.collate(g, w[1]);
//        assertThat(g, graph(w[0]).aligned("the", "same", "stuff"));
//        assertThat(g, graph(w[1]).aligned("the", "same", "stuff"));
    }

    @Ignore
    @Test
    public void testCaseVariantGraphTwoDifferentWitnesses() {
        final SimpleWitness[] w = createWitnesses("The quick brown fox jumps over the lazy dog", "The fast brown fox jumps over the black dog");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        VariantGraph g = new VariantGraph();
        aligner.collate(g, w);

        assertThat(g, graph(w[0]).aligned("the").non_aligned("quick").aligned("brown", "fox", "jumps", "over", "the").non_aligned("lazy").aligned("dog"));
        assertThat(g, graph(w[1]).aligned("the").non_aligned("fast").aligned("brown", "fox", "jumps", "over", "the").non_aligned("black").aligned("dog"));
    }


}
