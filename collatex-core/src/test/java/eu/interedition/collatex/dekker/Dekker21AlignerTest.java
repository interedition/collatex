package eu.interedition.collatex.dekker;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex.util.VariantGraphTraversal;
import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Dekker21Aligner.ExtendedGraphNode;
import eu.interedition.collatex.simple.SimpleWitness;

import static eu.interedition.collatex.dekker.VariantGraphMatcher.graph;
import static org.junit.Assert.*;

public class Dekker21AlignerTest extends AbstractTest {

    private void assertLCP_Interval(int start, int length, int depth, LCP_Interval lcp_interval) {
        assertEquals(start, lcp_interval.start);
        assertEquals(length, lcp_interval.length);
        assertEquals(depth, lcp_interval.depth());
    }

    private void assertNode(int i, int j, ExtendedGraphNode decisionGraphNode) {
        assertEquals(i, decisionGraphNode.startPosWitness1);
        assertEquals(j, decisionGraphNode.startPosWitness2);
    }

    private void assertMatch(int i, int j, boolean match, ExtendedGraphNode decisionGraphNode) {
        assertEquals(i, decisionGraphNode.startPosWitness1);
        assertEquals(j, decisionGraphNode.startPosWitness2);
        assertEquals(match, decisionGraphNode.isMatch());
    }

    private void debugPath(Dekker21Aligner.ThreeDimensionalDecisionGraph decisionGraph, List<Dekker21Aligner.ExtendedGraphNode> decisionGraphNodes) {
        for (ExtendedGraphNode node : decisionGraphNodes) {
            System.out.println(node.startPosWitness1 + ":" + node.startPosWitness2);
            for (Dekker21Aligner.ExtendedGraphNode neighbor : decisionGraph.neighborNodes(node)) {
                Dekker21Aligner.DecisionGraphNodeCost decisionGraphNodeCost = decisionGraph.distBetween(node, neighbor);
                Dekker21Aligner.DecisionGraphNodeCost heuristic = decisionGraph.heuristicCostEstimate(neighbor);
                System.out.println(">> neighbor: "+neighbor.startPosWitness1+","+neighbor.startPosWitness2+":"+decisionGraphNodeCost.alignedTokens+":"+heuristic.alignedTokens);
            }
        }
    }

    @Test
    public void testCaseDanielStoekl() {
        // 1: a, b, c, d, e
        // 2: a, e, c, d
        // 3: a, d, b
        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        //Note: the suffix array can have multiple forms
        //outcome of sorting is not guaranteed
        //however the LCP array is fixed we can assert that
        assertEquals("[-1, 1, 1, 0, 1, 0, 2, 0, 1, 1, 0, 1]", Arrays.toString(aligner.LCP_array));
    }

    @Test
    public void testCaseDanielStoeklLCPIntervals() {
        // 1: a, b, c, d, e
        // 2: a, e, c, d
        // 3: a, d, b
        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        List<LCP_Interval> lcp_intervals = aligner.tokenIndex.splitLCP_ArrayIntoIntervals();
        assertLCP_Interval(0, 1, 3, lcp_intervals.get(0)); // a
        assertLCP_Interval(3, 1, 2, lcp_intervals.get(1)); // b
        assertLCP_Interval(5, 2, 2, lcp_intervals.get(2)); // c d
        assertLCP_Interval(7, 1, 3, lcp_intervals.get(3)); // d
        assertLCP_Interval(10, 1, 2, lcp_intervals.get(4)); // e
        assertEquals(5, lcp_intervals.size());
    }

    @Test
    public void testExtendedGraphNodeIsAValueObject() {
        VariantGraph v = new VariantGraph();

        ExtendedGraphNode n1 = new ExtendedGraphNode(0, v.getStart(), 0);
        ExtendedGraphNode n2 = new ExtendedGraphNode(0, v.getStart(), 0);
        ExtendedGraphNode n3 = new ExtendedGraphNode(1, v.getEnd(), 0);
        ExtendedGraphNode n4 = new ExtendedGraphNode(0, v.getStart(), 1);
        assertTrue(n1.equals(n2));
        assertFalse(n1.equals(n3));
        assertFalse(n1.equals(n4));
    }

    @Test
    public void testCaseDecisionGraphIsGoalNode() {
        // 1: a, b, c, d, e
        // 2: a, e, c, d
        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        VariantGraph against = new VariantGraph();
        aligner.collate(against, w[0]);
        Dekker21Aligner.ThreeDimensionalDecisionGraph decisionGraph = aligner.createDecisionGraph(against, w[1]);

        ExtendedGraphNode root = new ExtendedGraphNode(0, against.getStart(), 0);
        ExtendedGraphNode goal = new ExtendedGraphNode(6, against.getEnd(), 4);
        //TODO: add neighbor node assertation
        //ExtendedGraphNode neighbor = new ExtendedGraphNode(1, 1);
        assertFalse(decisionGraph.isGoal(root));
        assertTrue(decisionGraph.isGoal(goal));
//        assertFalse(gr.isGoal(neighbor));
    }


//    @Test
//    public void testCaseDecisionGraphNeighbours() {
//        // 1: a, b, c, d, e
//        // 2: a, e, c, d
//        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d");
//        Dekker21Aligner aligner = new Dekker21Aligner(w);
//        VariantGraph against = new VariantGraph();
//        aligner.collate(against, w);
//
//        ExtendedGraphNode root = new ExtendedGraphNode(0, against.getStart(), 0);
//        Dekker21Aligner.ThreeDimensionalDecisionGraph gr = aligner.getDecisionGraph();
//
//        Iterator<ExtendedGraphNode> neighbors = gr.neighborNodes(root).iterator();
//        //TODO: this (0,0) is not needed! Filter out self
//        assertNode(0, 0, Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE, neighbors.next());
//        assertNode(1, 0, Dekker21Aligner.EditOperationEnum.SKIP_TOKEN_GRAPH, neighbors.next());
//        assertNode(0, 1, Dekker21Aligner.EditOperationEnum.SKIP_TOKEN_WITNESS, neighbors.next());
//        assertNode(1, 1, Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE, neighbors.next());
//        assertFalse(neighbors.hasNext());
//    }



//    @Test
//    public void testCaseDecisionGraphHeuristicCostFunction() {
//        // 1: a, b, c, d, e
//        // 2: a, e, c, d
//        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d");
//        Dekker21Aligner aligner = new Dekker21Aligner(w);
//        VariantGraph against = new VariantGraph();
//        aligner.collate(against, w);
//        DecisionGraphNode root = aligner.new DecisionGraphNode();
//        Dekker21Aligner.ThreeDimensionalDecisionGraph decisionGraph = aligner.getDecisionGraph();
//
//        Iterator<DecisionGraphNode> neighbours = decisionGraph.neighborNodes(root).iterator();
//        assertEquals(3, decisionGraph.heuristicCostEstimate(neighbours.next()).alignedTokens);
//        assertEquals(3, decisionGraph.heuristicCostEstimate(neighbours.next()).alignedTokens);
//        assertEquals(3, decisionGraph.heuristicCostEstimate(neighbours.next()).alignedTokens);
//    }
//
//    @Test
//    public void testCaseDecisionGraphdistanceFunction() {
//        // 1: a, b, c, d, e
//        // 2: a, e, c, d
//        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d");
//        Dekker21Aligner aligner = new Dekker21Aligner(w);
//        VariantGraph against = new VariantGraph();
//        aligner.collate(against, w);
//        DecisionGraphNode root = aligner.new DecisionGraphNode();
//        Dekker21Aligner.TwoDimensionalDecisionGraph decisionGraph = aligner.getDecisionGraph();
//
//        Iterator<DecisionGraphNode> neighbours = decisionGraph.neighborNodes(root).iterator();
//        assertEquals(0, decisionGraph.distBetween(root, neighbours.next()).alignedTokens);
//        assertEquals(0, decisionGraph.distBetween(root, neighbours.next()).alignedTokens);
//        assertEquals(1, decisionGraph.distBetween(root, neighbours.next()).alignedTokens);
//    }
//
//    @Test
//    public void testCaseDecisionGraphPath() {
//        // 1: a, b, c, d, e
//        // 2: a, e, c, d
//        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d");
//        Dekker21Aligner aligner = new Dekker21Aligner(w);
//        VariantGraph against = new VariantGraph();
//        aligner.collate(against, w);
//        DecisionGraphNode root = aligner.new DecisionGraphNode();
//        Dekker21Aligner.ThreeDimensionalDecisionGraph decisionGraph = aligner.getDecisionGraph();
//
//        List<DecisionGraphNode> decisionGraphNodes = decisionGraph.aStar(aligner.new DecisionGraphNode(), aligner.new DecisionGraphNodeCost(0));
//        Iterator<DecisionGraphNode> nodes = decisionGraphNodes.iterator();
//        assertNode(0, 0, null, nodes.next());
//        assertNode(1, 1, Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE, nodes.next());
//        assertNode(2, 2, Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE, nodes.next());
//        assertNode(3, 3, Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE, nodes.next());
//        assertNode(4, 4, Dekker21Aligner.EditOperationEnum.MATCH_TOKENS_OR_REPLACE, nodes.next());
//        assertNode(5, 4, Dekker21Aligner.EditOperationEnum.SKIP_TOKEN_GRAPH, nodes.next());
//        assertFalse(nodes.hasNext());
//    }
//
//    @Test
//    public void testCaseDecisionGraphMatches() {
//        // 1: a, b, c, d, e
//        // 2: a, e, c, d
//        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d");
//        Dekker21Aligner aligner = new Dekker21Aligner(w);
//        VariantGraph against = new VariantGraph();
//        aligner.collate(against, w);
//        DecisionGraphNode root = aligner.new DecisionGraphNode();
//        Dekker21Aligner.TwoDimensionalDecisionGraph decisionGraph = aligner.getDecisionGraph();
//
//        List<DecisionGraphNode> decisionGraphNodes = decisionGraph.aStar(aligner.new DecisionGraphNode(), aligner.new DecisionGraphNodeCost(0));
//        Iterator<DecisionGraphNode> nodes = decisionGraphNodes.iterator();
//        assertMatch(0, 0, false, nodes.next());
//        assertMatch(1, 1, true, nodes.next());
//        assertMatch(2, 2, false, nodes.next());
//        assertMatch(3, 3, true, nodes.next());
//        assertMatch(4, 4, true, nodes.next());
//        assertMatch(5, 4, false, nodes.next());
//        assertFalse(nodes.hasNext());
//    }
//
//    @Test
//    public void testCaseVariantGraphOneWitness() {
//        // 1: a, b, c, d, e
//        final SimpleWitness[] w = createWitnesses("a b c d e");
//        Dekker21Aligner aligner = new Dekker21Aligner(w);
//        VariantGraph g = new VariantGraph();
//        aligner.collate(g, w);
//
//        assertThat(g, graph(w[0]).non_aligned("a", "b", "c", "d", "e"));
//    }
//
//    @Test
//    public void testCaseVariantGraphTwoWitnesses() {
//        // 1: a, b, c, d, e
//        // 2: a, e, c, d
//        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d");
//        Dekker21Aligner aligner = new Dekker21Aligner(w);
//        VariantGraph g = new VariantGraph();
//        aligner.collate(g, w);
//
//        assertThat(g, graph(w[0]).aligned("a").non_aligned("b").aligned("c", "d").non_aligned("e"));
//        assertThat(g, graph(w[1]).aligned("a").non_aligned("e").aligned("c", "d"));
//    }
//

//    @Test
//    public void testCaseDecisionGraphThreeWitnessesMatches() {
//        final SimpleWitness[] w = createWitnesses("The quick brown fox jumps over the lazy dog", "The fast brown fox jumps over the black dog", "The red fox jumps over the fence");
//        Dekker21Aligner aligner = new Dekker21Aligner(w);
//        VariantGraph g = new VariantGraph();
//        // we collate the first two witnesses
//        aligner.collate(g, w[0]);
//        aligner.collate(g, w[1]);
//        // then we examine the decision graph for the third witness
//        Dekker21Aligner.TwoDimensionalDecisionGraph decisionGraph = aligner.createDecisionGraph(g, w[2]);
//        List<DecisionGraphNode> decisionGraphNodes = decisionGraph.aStar(aligner.new DecisionGraphNode(), aligner.new DecisionGraphNodeCost());
//        Iterator<DecisionGraphNode> nodes = decisionGraphNodes.iterator();
//        assertMatch(0, 0, false, nodes.next());
//        assertMatch(1, 1, true, nodes.next());
//        assertMatch(2, 2, false, nodes.next());
//        assertMatch(3, 3, true, nodes.next());
//        assertMatch(4, 4, true, nodes.next());
//        assertMatch(5, 4, false, nodes.next());
//        assertFalse(nodes.hasNext());
//    }

//    @Test
//    public void testCaseVariantGraphThreeWitnesses() {
//        final SimpleWitness[] w = createWitnesses("The quick brown fox jumps over the lazy dog", "The fast brown fox jumps over the black dog", "The red fox jumps over the fence");
//        Dekker21Aligner aligner = new Dekker21Aligner(w);
//        VariantGraph g = new VariantGraph();
//        aligner.collate(g, w);
//
//        assertThat(g, graph(w[0]).aligned("the").non_aligned("quick").aligned("brown", "fox", "jumps", "over", "the").non_aligned("lazy").aligned("dog"));
//        assertThat(g, graph(w[1]).aligned("the").non_aligned("fast").aligned("brown", "fox", "jumps", "over", "the").non_aligned("black").aligned("dog"));
//        assertThat(g, graph(w[2]).aligned("the").non_aligned("red").aligned("fox", "jumps", "over", "the").non_aligned("fence"));
//    }
//
//
//
//    @Test
//    public void test3dMatching1() {
//        SimpleWitness[] witnesses = createWitnesses("a", "b", "c", "a b c");
//        VariantGraph g = new VariantGraph();
//        new Dekker21Aligner(witnesses).collate(g, witnesses);
//        assertThat(g, graph(witnesses[3]).aligned("a", "b", "c"));
//    }

}
