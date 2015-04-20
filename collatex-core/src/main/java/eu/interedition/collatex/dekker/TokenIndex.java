package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;

import java.util.*;

/**
 * Created by ronald on 4/20/15.
 */
public class TokenIndex {
    //TODO: not sure this functionality should be in this class or in a separate class
    private Map<Witness, Integer> witnessToStartToken;
    private final SimpleWitness[] w;
    protected List<Token> token_array;
    //END witness data
    public int[] suffix_array;
    public int[] LCP_array;
    public List<LCP_Interval> lcp_intervals;
    private LCP_Interval[] lcp_interval_array;
    private final Dekker21Aligner aligner;

    public TokenIndex(Dekker21Aligner aligner, SimpleWitness[] w) {
        this.aligner = aligner;
        this.w = w;
    }

    public int getStartTokenPositionForWitness(Witness witness) {
        return witnessToStartToken.get(witness);
    }

    // 1. prepare token array
    // 2. derive the suffix array
    // 3. derive LCP array
    // 4. derive LCP intervals
    public void prepare() {
        this.prepareTokenArray();
        Comparator<Token> comparator = new SimpleTokenNormalizedFormComparator();
        SuffixData suffixData = SuffixArrays.createWithLCP(token_array.toArray(new Token[0]), new SAIS(), comparator);
        this.suffix_array = suffixData.getSuffixArray();
        this.LCP_array = suffixData.getLCP();
        this.lcp_intervals = splitLCP_ArrayIntoIntervals();
        lcp_interval_array = construct_LCP_interval_array();
    }

    private void prepareTokenArray() {
        token_array = new ArrayList<>();
        int counter = 0;
        witnessToStartToken = new HashMap<>();
        for (SimpleWitness witness : w) {
            witnessToStartToken.put(witness, counter);
            for (Token t : witness) {
                token_array.add(t);
                counter++;
            }
            //TODO: add witness separation marker token
        }
    }

    protected List<LCP_Interval> splitLCP_ArrayIntoIntervals() {
        List<LCP_Interval> closedIntervals = new ArrayList<>();
        int previousLCP_value = 0;
        Stack<LCP_Interval> openIntervals = new Stack<LCP_Interval>();
        for (int idx = 0; idx < LCP_array.length; idx++) {
            int lcp_value = LCP_array[idx];
            if (lcp_value > previousLCP_value) {
                openIntervals.push(new LCP_Interval(idx - 1, lcp_value));
                previousLCP_value = lcp_value;
            } else if (lcp_value < previousLCP_value) {
                // close open intervals that are larger than current LCP value
                while (!openIntervals.isEmpty() && openIntervals.peek().length > lcp_value) {
                    LCP_Interval a = openIntervals.pop();
                    closedIntervals.add(new LCP_Interval(a.start, idx - 1, a.length));
                }
                // then: open a new interval starting with filtered intervals
                if (lcp_value > 0) {
                    int start = closedIntervals.get(closedIntervals.size() - 1).start;
                    openIntervals.add(new LCP_Interval(start, lcp_value));
                }
                previousLCP_value = lcp_value;
            }
        }
        // add all the open intervals to the result
        for (LCP_Interval interval : openIntervals) {
            closedIntervals.add(new LCP_Interval(interval.start, LCP_array.length - 1, interval.length));
        }
        return closedIntervals;
    }

    private LCP_Interval[] construct_LCP_interval_array() {
        LCP_Interval[] lcp_interval_array = new LCP_Interval[token_array.size()];
        for (LCP_Interval interval : lcp_intervals) {
            //TODO: why are there empty LCP intervals in the LCP_interval_array ?
            if (interval.length==0) {
                continue;
            }
            for (int i = interval.start; i <= interval.end; i++) {
                int tokenPosition = suffix_array[i];
                //Log("Adding interval: " + interval.toString() + " to token number: " + tokenIndex);
                lcp_interval_array[tokenPosition] = interval;
            }
        }
//        //NOTE: For tokens that are not repeated we create new LCP intervals here
//        //This is not very space efficient, but it makes life much easier for the code that follows
//        for (int i=0; i< this.token_array.size(); i++) {
//            if (lcp_interval_array[i]==null) {
//                // create new LCP interval for token
//                LCP_Interval lcp_interval;
//                //NOTE: I have to know the start and end position of token in the suffix array... not easy!
//                lcp_interval = new LCP_Interval()
//            }
//        }
        return lcp_interval_array;
    }


    public LCP_Interval getLCP_intervalFor(int tokenPosition) {
        return lcp_interval_array[tokenPosition];
    }

    public boolean hasLCP_intervalFor(int i) {
        return lcp_interval_array[i]!=null;
    }
}
