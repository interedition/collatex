package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

/**
 * Created by ronald on 4/20/15.
 */
public class TokenIndex {
    private final Dekker21Aligner aligner;
    public List<LCP_Interval> lcp_intervals;
    public int[] LCP_array;
    public int[] suffix_array;

    public TokenIndex(Dekker21Aligner aligner) {
        this.aligner = aligner;
    }

    public void prepare() {
        Comparator<Token> comparator = new SimpleTokenNormalizedFormComparator();
        SuffixData suffixData = SuffixArrays.createWithLCP(aligner.token_array.toArray(new Token[0]), new SAIS(), comparator);
        this.suffix_array = suffixData.getSuffixArray();
        this.LCP_array = suffixData.getLCP();
        this.lcp_intervals = splitLCP_ArrayIntoIntervals();
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

}
