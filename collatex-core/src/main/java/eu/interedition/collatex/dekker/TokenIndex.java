package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by ronald on 4/20/15.
 */
public class TokenIndex {
    //TODO: not sure this functionality should be in this class or in a separate class
    private Map<Witness, Integer> witnessToStartToken;
    private Map<Witness, Integer> witnessToEndToken;
    private final List<? extends Iterable<Token>> w;
    protected List<Token> token_array;
    //END witness data
    public int[] suffix_array;
    public int[] LCP_array;
    public List<Block> blocks;
    private Block[] block_array;


    public TokenIndex(List<? extends Iterable<Token>> w) {
        this.w = w;
    }

    public TokenIndex(Iterable<Token>[] w) {
        List<Iterable<Token>> witnesses = Arrays.asList(w);
        this.w = witnesses;
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
        this.blocks = splitLCP_ArrayIntoIntervals();
        block_array = construct_LCP_interval_array();
    }

    private void prepareTokenArray() {
        token_array = new ArrayList<>();
        int counter = 0;
        witnessToStartToken = new HashMap<>();
        witnessToEndToken = new HashMap<>();
        for (Iterable<Token> tokens : w) {
            Witness witness = tokens.iterator().next().getWitness();
            witnessToStartToken.put(witness, counter);
            for (Token t : tokens) {
                token_array.add(t);
                counter++;
            }
            witnessToEndToken.put(witness, counter);
            //TODO: add witness separation marker token
        }
    }

    protected List<Block> splitLCP_ArrayIntoIntervals() {
        List<Block> closedIntervals = new ArrayList<>();
        int previousLCP_value = 0;
        Stack<Block> openIntervals = new Stack<Block>();
        for (int idx = 0; idx < LCP_array.length; idx++) {
            int lcp_value = LCP_array[idx];
            if (lcp_value > previousLCP_value) {
                openIntervals.push(new Block(this, idx - 1, lcp_value));
                previousLCP_value = lcp_value;
            } else if (lcp_value < previousLCP_value) {
                // close open intervals that are larger than current LCP value
                while (!openIntervals.isEmpty() && openIntervals.peek().length > lcp_value) {
                    Block a = openIntervals.pop();
                    closedIntervals.add(new Block(this, a.start, idx - 1, a.length));
                }
                // then: open a new interval starting with filtered intervals
                if (lcp_value > 0) {
                    int start = closedIntervals.get(closedIntervals.size() - 1).start;
                    openIntervals.add(new Block(this, start, lcp_value));
                }
                previousLCP_value = lcp_value;
            }
        }
        // add all the open intervals to the result
        for (Block interval : openIntervals) {
            closedIntervals.add(new Block(this, interval.start, LCP_array.length - 1, interval.length));
        }
        return closedIntervals;
    }

    private Block[] construct_LCP_interval_array() {
        Block[] block_array = new Block[token_array.size()];
        for (Block interval : blocks) {
            //TODO: why are there empty LCP intervals in the LCP_interval_array ?
            if (interval.length==0) {
                continue;
            }
            for (int i = interval.start; i <= interval.end; i++) {
                int tokenPosition = suffix_array[i];
                //Log("Adding interval: " + interval.toString() + " to token number: " + tokenIndex);
                block_array[tokenPosition] = interval;
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
        return block_array;
    }


    public Block getLCP_intervalFor(int tokenPosition) {
        return block_array[tokenPosition];
    }

    public boolean hasLCP_intervalFor(int i) {
        return block_array[i]!=null;
    }

    // lcp intervals can overlap horizontally
    // we prioritize the intervals with the biggest length
    // Note: with more than two witnesses we have to select the right instance of an interval
    public List<Block> getNonOverlappingBlocks() {
        // sort lcp intervals based on length in descending order
        Collections.sort(blocks, (Block interval1, Block interval2) -> interval2.length - interval1.length);
        //TODO: set size based on the length of the token array
        BitSet occupied = new BitSet();
        // set up predicate
        // why is length check needed? empty lcp intervals should not be there
        Predicate<Block> p = lcp_interval -> lcp_interval.length > 0 && !lcp_interval.getAllOccurrencesAsRanges().anyMatch(i -> occupied.get(i));

        List<Block> result = new ArrayList<>();
        for (Block interval : blocks) {
            // test whether the interval is in occupied
            //Note: filter
            if (p.test(interval)) {
                result.add(interval);
                // mark all the occurrences of the lcp interval in the occupied bit set
                interval.getAllOccurrencesAsRanges().forEach(occupied::set);
            }
        }
        return result;
    }

    //NOTE: Much of the work done here can be prepared before hand
    //and stored in the lcp_array.. should be transformed in a block instance array
    public List<Block.Instance> getBlockInstancesForWitness(Witness w) {
        Integer witnessStart = witnessToStartToken.get(w);
        Integer witnessEnd = witnessToEndToken.get(w);
        //TODO: size: witnessEnd!
        BitSet witnessRange = new BitSet();
        witnessRange.set(witnessStart, witnessEnd);
        //TODO: rewrite!
        List<Block.Instance> result = new ArrayList<>();
        for (Block block : getNonOverlappingBlocks()) {
            for (Block.Instance instance : block.getAllInstances()) {
                if (instance.asRange().anyMatch(witnessRange::get)) {
                    result.add(instance);
                }
            }
        }
        result.sort((instance1, instance2) -> instance1.start_token - instance2.start_token);
        return result;
    }
}
