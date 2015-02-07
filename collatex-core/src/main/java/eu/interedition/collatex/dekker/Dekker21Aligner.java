package eu.interedition.collatex.dekker;

import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;

public class Dekker21Aligner extends CollationAlgorithm.Base {
	
	private List<Token> token_array;
  private int[] suffix_array;
  protected int[] LCP_array;
	

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

	@Override
	public void collate(VariantGraph against, Iterable<Token> witness) {
		// TODO Auto-generated method stub
		
	}

}
