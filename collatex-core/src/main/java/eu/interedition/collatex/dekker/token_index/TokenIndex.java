package eu.interedition.collatex.dekker.token_index;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.scs.TokenArray;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;

import java.util.*;

/**
 * Created by ronald on 4/20/15.
 */
public class TokenIndex extends TokenArray {
    private final List<? extends Iterable<Token>> witnesses;
    private final Comparator<Token> comparator;
    //TODO: not sure this functionality should be in this class or in a separate class
    //END witness data
    public int[] suffix_array;
    public int[] LCP_array;
    public List<Block> blocks;
    private Map<Witness, List<Block.Instance>> witnessToBlockInstances;


    public TokenIndex(Comparator<Token> comparator, Iterable<Token>... witness) {
        this(comparator, Arrays.asList(witness));
    }

    public TokenIndex(Comparator<Token> comparator, List<? extends Iterable<Token>> witnesses) {
        this.witnesses = witnesses;
        this.comparator = new MarkerTokenComparator(comparator);
    }

    public int getStartTokenPositionForWitness(Witness witness) {
        return witnessToStartToken.get(witness);
    }

    // 1. prepare token array
    // 2. derive the suffix array
    // 3. derive LCP array
    // 4. derive LCP intervals
    // TODO: we do not have to store witnesses!
    public void prepare() {
        token_array = this.prepareTokenArray(this.witnesses);
        SuffixData suffixData = SuffixArrays.createWithLCP(token_array, new SAIS(), comparator);
        this.suffix_array = suffixData.getSuffixArray();
        this.LCP_array = suffixData.getLCP();
        this.blocks = splitLCP_ArrayIntoIntervals();
        constructWitnessToBlockInstancesMap();
    }


    protected List<Block> splitLCP_ArrayIntoIntervals() {
        List<Block> closedIntervals = new ArrayList<>();
        int previousLCP_value = 0;
        Stack<Block> openIntervals = new Stack<>();
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
                    openIntervals.push(new Block(this, start, lcp_value));
                }
                previousLCP_value = lcp_value;
            }
        }
        // add all the open intervals to the result
        for (Block interval : openIntervals) {
            if (interval.length > 0) {
                closedIntervals.add(new Block(this, interval.start, LCP_array.length - 1, interval.length));
            }
        }
        return closedIntervals;
    }

    private void constructWitnessToBlockInstancesMap() {
        witnessToBlockInstances = new HashMap<>();
        for (Block interval : blocks) {
            for (Block.Instance instance : interval.getAllInstances()) {
                Witness w = instance.getWitness();
                List<Block.Instance> instances = witnessToBlockInstances.computeIfAbsent(w, v -> new ArrayList<>());
                instances.add(instance);
            }
        }
    }

    //NOTE: An empty list is returned when there are no instances for the specified witness
    public List<Block.Instance> getBlockInstancesForWitness(Witness w) {
        return witnessToBlockInstances.computeIfAbsent(w, v -> Collections.emptyList());
    }

    public int size() {
        return token_array.length;
    }
}
