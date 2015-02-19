package eu.interedition.collatex.dekker;

import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;

public class Dekker21Aligner extends CollationAlgorithm.Base {
	
	private List<Token> token_array;
  private int[] suffix_array;
  protected int[] LCP_array;
  private DecisionGraph g;
  private HeuristicCostFunction heuristic;

	public Dekker21Aligner(SimpleWitness[] w) {
		// 1. prepare token array
		// 2. derive the suffix array 
		// 3. derive LCP array
		// 4. derive LCP intervals
		token_array = Lists.newArrayList();
		for (SimpleWitness witness : w) {
			for (Token t : witness) {
				token_array.add(t);
			}
			//TODO: add witness separation marker token
		}
		Comparator<Token> comparator = new SimpleTokenNormalizedFormComparator();
		SuffixData suffixData = SuffixArrays.createWithLCP(token_array.toArray(new Token[0]), new SAIS(), comparator);
		suffix_array = suffixData.getSuffixArray();
		LCP_array = suffixData.getLCP();
	}

	protected List<LCP_Interval> splitLCP_ArrayIntoIntervals() {
	  List<LCP_Interval> closedIntervals = Lists.newArrayList();
	  int previousLCP_value = 0;
	  Stack<LCP_Interval> openIntervals = new Stack<LCP_Interval>();
	  for (int idx = 0; idx < LCP_array.length; idx++) {
	    int lcp_value = LCP_array[idx];
	    if (lcp_value > previousLCP_value) {
	      openIntervals.push(new LCP_Interval(idx-1, lcp_value));
	      previousLCP_value = lcp_value;
	    }
	    else if (lcp_value < previousLCP_value) {
	      // close open intervals that are larger than current LCP value
	      while ( !openIntervals.isEmpty() && openIntervals.peek().length > lcp_value ) {
	        LCP_Interval a = openIntervals.pop();
	        closedIntervals.add(new LCP_Interval(a.start, idx-1, a.length));
	      }
	      // then: open a new interval starting with filtered intervals
	      if (lcp_value > 0) {
	        int start = closedIntervals.get(closedIntervals.size()-1).start;
	        openIntervals.add(new LCP_Interval(start, lcp_value));
	      }
	      previousLCP_value = lcp_value;
	    }
	  }
	  // add all the open intervals to the result
	  for (LCP_Interval interval : openIntervals) {
	    closedIntervals.add(new LCP_Interval(interval.start, this.LCP_array.length-1, interval.length));
	  }
	  return closedIntervals;
	}
	
  protected String debug(LCP_Interval interval) {
    int suffix_start = interval.start;
    int token_pos = this.suffix_array[suffix_start];
    //TODO; add more tokens (look at length)
    Token t = this.token_array.get(token_pos);
    return interval.toString()+" -> "+t.toString();
  }

  protected DecisionGraph getDecisionGraph() {
    if (g==null) {
      throw new IllegalStateException("Collate something first!");
    }
    return g;
  }

  protected HeuristicCostFunction getHeuristic() {
    if (heuristic==null) {
      throw new IllegalStateException("Collate something first!");
    }
    return heuristic;
  }


	@Override
	public void collate(VariantGraph against, Iterable<Token> witness) {
		List<LCP_Interval> LCP_intervals = splitLCP_ArrayIntoIntervals();
		// map LCP intervals to range, so that for each position in
		// the token array the LCP interval can be found
		RangeMap<Integer, LCP_Interval> rm = TreeRangeMap.create();
		for (LCP_Interval interval : LCP_intervals) {
		  Range<Integer> ri = Range.closed(interval.start, interval.end);
		  rm.put(ri, interval);
		}
		// now we are going to prepare a* mechanism
		// 1) We need to know the neighbors in the decision graph
		// There are three possibilities 
		// 2) We need to calculate the heuristic for each
		// 3) We need to score each
		// we need to know how long the witnesses are
		int lengthWitness1 = 5; // [0, 4]
		int lengthWitness2 = 4; // [5, 8]
		int beginWitness1 = 0;
		int endWitness1 = 4;
		int beginWitness2 = 5;
		int endWitness2 = 8;
		// set default matcher
		Matcher matcher = new SimpleMatcher();
		// try to find a match
		// are they the same interval?
    g = new DecisionGraph();
    heuristic = new HeuristicCostFunction(rm, beginWitness1, endWitness1, beginWitness2, endWitness2);
	}
	
  

	
	class DecisionGraph {
	  private DecisionGraphNode root;
	  
	  public DecisionGraph() {
	    root = new DecisionGraphNode();
	  }
	  
    //setEditOperation
    // in het geval child3 moet ik eerst matchen om te kunnen
    // scoren van de node
	  public List<DecisionGraphNode> neighbours(DecisionGraphNode current) {
	    // 3 possibilities
	    DecisionGraphNode child1 = current.copy();
	    DecisionGraphNode child2 = current.copy();
	    DecisionGraphNode child3 = current.copy();
	    
      child1.startPosWitness1++;
	    child2.startPosWitness2++;
      child3.startPosWitness1++;
      child3.startPosWitness2++;

	    List<DecisionGraphNode> children = Lists.newArrayList();
	    children.add(child1);
	    children.add(child2);
	    children.add(child3);
	    
	    return children;
	  }

    public DecisionGraphNode getRoot() {
      return root;
    }
	}
	
	class DecisionGraphNode {
    int startPosWitness1 = 0;
    int startPosWitness2 = 0;
    int alignedTokens = 0; // cost function TODO: this is far too simple!
    public DecisionGraphNode copy() {
      DecisionGraphNode copy = new DecisionGraphNode();
      copy.startPosWitness1 = this.startPosWitness1;
      copy.startPosWitness2 = this.startPosWitness2;
      copy.alignedTokens = this.alignedTokens;
      return copy;
    }
	  
	}
	
  class Matcher {
	  Boolean match(Token a, Token b) {
	    return false;
	  }
	  
	}
	
	class SimpleMatcher extends Matcher {
	  @Override
	  Boolean match(Token a, Token b) {
	    SimpleToken sa = (SimpleToken) a;
	    SimpleToken sb = (SimpleToken) b;
	    return sa.getNormalized().equals(sb.getNormalized());
	  }
	}
	
	class HeuristicCostFunction {
	  private RangeMap<Integer, LCP_Interval> rm;
    private int startRangeWitness1;
    private int endRangeWitness1;
    private int startRangeWitness2;
    private int endRangeWitness2;

    public HeuristicCostFunction(RangeMap<Integer, LCP_Interval> rm, int startRangeWitness1, int endRangeWitness1, int startRangeWitness2, int endRangeWitness2) {
      this.rm = rm;
      this.startRangeWitness1 = startRangeWitness1;
      this.endRangeWitness1 = endRangeWitness1;
      this.startRangeWitness2 = startRangeWitness2;
      this.endRangeWitness2 = endRangeWitness2;
	  }
	  
	  protected int heuristic(DecisionGraphNode node) {
	    int minimum_wit1 = 0;
	    for (int i = startRangeWitness1 + node.startPosWitness1; i <= endRangeWitness1; i++) {
	      if (rm.get(i)!=null) {
	        minimum_wit1 ++;
	      }
	    }
	    int minimum_wit2 = 0;
	    for (int i = startRangeWitness2 + node.startPosWitness2; i <= endRangeWitness2; i++) {
	      if (rm.get(i)!=null) {
	        minimum_wit2 ++;
	      }
	    }
	    int potential = Math.min(minimum_wit1, minimum_wit2);
	    return potential;
	  }
	}
}
