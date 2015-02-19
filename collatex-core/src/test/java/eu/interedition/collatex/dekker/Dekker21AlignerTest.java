package eu.interedition.collatex.dekker;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Dekker21Aligner.DecisionGraph;
import eu.interedition.collatex.dekker.Dekker21Aligner.DecisionGraphNode;
import eu.interedition.collatex.dekker.Dekker21Aligner.HeuristicCostFunction;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;

public class Dekker21AlignerTest extends AbstractTest {

  private void assertLCP_Interval(int start, int length, int depth, LCP_Interval lcp_interval) {
    assertEquals(start, lcp_interval.start);
    assertEquals(length, lcp_interval.length);
    assertEquals(depth, lcp_interval.depth());
  }
  
  private void assertNode(int i, int j, DecisionGraphNode decisionGraphNode) {
    assertEquals(i, decisionGraphNode.startPosWitness1);
    assertEquals(j, decisionGraphNode.startPosWitness2);
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
    List<LCP_Interval> lcp_intervals = aligner.splitLCP_ArrayIntoIntervals();
    assertLCP_Interval(0, 1, 3, lcp_intervals.get(0)); // a
    assertLCP_Interval(3, 1, 2, lcp_intervals.get(1)); // b
    assertLCP_Interval(5, 2, 2, lcp_intervals.get(2)); // c d
    assertLCP_Interval(7, 1, 3, lcp_intervals.get(3)); // d
    assertLCP_Interval(10, 1, 2, lcp_intervals.get(4)); // e
    assertEquals(5, lcp_intervals.size());
  }
  
  @Test
  public void testCaseDanielStoeklDecisionGraph() {
    // 1: a, b, c, d, e
    // 2: a, e, c, d
    // 3: a, d, b
    final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
    Dekker21Aligner aligner = new Dekker21Aligner(w);
    VariantGraph against = new JungVariantGraph();
    aligner.collate(against, w);
    
    DecisionGraph gr = aligner.getDecisionGraph();
    // NOTE: active form: expand tree
    List<DecisionGraphNode> neighbours = gr.neighbours(gr.getRoot());
    assertEquals(3, neighbours.size());
    assertNode(1,0, neighbours.get(0));
    assertNode(0,1, neighbours.get(1));
    assertNode(1,1, neighbours.get(2));
    
  }

  @Test
  public void testCaseDanielStoeklheuristicCostFunction() {
    // 1: a, b, c, d, e
    // 2: a, e, c, d
    // 3: a, d, b
    final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
    Dekker21Aligner aligner = new Dekker21Aligner(w);
    VariantGraph against = new JungVariantGraph();
    aligner.collate(against, w);
    
    DecisionGraph gr = aligner.getDecisionGraph();
    List<DecisionGraphNode> neighbours = gr.neighbours(gr.getRoot());
    HeuristicCostFunction heuristic = aligner.getHeuristic();
    assertEquals(4, heuristic.heuristic(neighbours.get(0)));
    assertEquals(3, heuristic.heuristic(neighbours.get(1)));
    assertEquals(3, heuristic.heuristic(neighbours.get(2)));
  }

}
