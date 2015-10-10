package eu.interedition.collatex.dekker.token_index;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * Created by ronald on 4/20/15.
 */
public class TokenIndex {
    private final List<? extends Iterable<Token>> w;
    private final Comparator<Token> comparator;
    //TODO: not sure this functionality should be in this class or in a separate class
    private Map<Witness, Integer> witnessToStartToken;
    private Map<Witness, Integer> witnessToEndToken;
    public List<Token> token_array;
    //END witness data
    public int[] suffix_array;
    public int[] LCP_array;
    public List<Block> blocks;
    private Map<Integer, List<Block.Instance>> block_array;


    public TokenIndex(Comparator<Token> comparator, Iterable<Token>... tokens) {
        this(comparator, Arrays.asList(tokens));
    }

    public TokenIndex(Comparator<Token> comparator, List<? extends Iterable<Token>> w) {
        this.w = w;
        this.comparator = new MarkerTokenComparatorWrapper(comparator);
    }

    public int getStartTokenPositionForWitness(Witness witness) {
        return witnessToStartToken.get(witness);
    }

    // 1. prepare token array
    // 2. derive the suffix array
    // 3. derive LCP array
    // 4. derive LCP intervals
    // TODO: we do not have to store w!
    public void prepare() {
        this.prepareTokenArray();
        //TODO: new TokenArray size is niet handig!
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
            final Witness witness = StreamSupport.stream(tokens.spliterator(), false)
                .findFirst()
                .map(Token::getWitness)
                .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

            witnessToStartToken.put(witness, counter);
            for (Token t : tokens) {
                token_array.add(t);
                counter++;
            }
            witnessToEndToken.put(witness, counter);
            token_array.add(new MarkerToken(witnessToStartToken.size()));
            counter++;
        }
    }

    private class MarkerToken implements Token {
        private final int witnessIdentifier;

        public MarkerToken(int size) {
            this.witnessIdentifier = size;
        }

        @Override
        public String toString() {
            return "$"+witnessIdentifier;
        }

        @Override
        public Witness getWitness() {
            throw new RuntimeException("A marker token is not part of any witness! The call to this method should never have happened!");
        }
    }

    static class MarkerTokenComparatorWrapper implements Comparator<Token> {
        private Comparator<Token> delegate;

        public MarkerTokenComparatorWrapper(Comparator<Token> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(Token o1, Token o2) {
            boolean o1isMarker = o1 instanceof MarkerToken;
            boolean o2isMarker = o2 instanceof MarkerToken;
            if (o1isMarker) {
                // Both o1 and o2 could be marker tokens
                if (o2isMarker) {
                    MarkerToken mt1 = (MarkerToken) o1;
                    MarkerToken mt2 = (MarkerToken) o2;
                    // sort marker tokens from low to high
                    return mt1.witnessIdentifier - mt2.witnessIdentifier;
                }
                // or one of them could be a marker token
                // always put the marker token before the content
                return -1;
            }
            // or one of them could be a marker token
            // always put the content after the marker token
            if (o2isMarker) {
                return 1;
            }
            // Not a marker token; call delegate
            return delegate.compare(o1, o2);
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
            if (interval.length > 0) {
                closedIntervals.add(new Block(this, interval.start, LCP_array.length - 1, interval.length));
            }
        }
        return closedIntervals;
    }

    private Map<Integer, List<Block.Instance>> construct_LCP_interval_array() {
        block_array = new HashMap<>();
        for (Block interval : blocks) {
            for (Block.Instance instance : interval.getAllInstances()) {
                int tokenPosition = instance.start_token;
                //System.out.println("Adding interval instance: " + interval.toString() + " to token number: " + tokenPosition);
                List<Block.Instance> values = block_array.get(tokenPosition);
                if (values == null) {
                    values = new ArrayList<>();
                    block_array.put(tokenPosition, values);
                }
                values.add(instance);
            }
        }
        return block_array;
    }


    public List<Block.Instance> getLCP_intervalFor(int tokenPosition) {
        return block_array.get(tokenPosition);
    }

    public boolean hasLCP_intervalFor(int i) {
        return block_array.containsKey(i);
    }

    // THIS SHOULD NOT BE HERE: IT CAN NOT BE DONE GLOBALLY!
    // THE OVERLAP SHOULD BE REMOVED DUE THE ALIGNING AND NOT SOME OTHER TRICK!
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

    public List<Block.Instance> getBlockInstancesForWitness(Witness w) {
        Integer witnessStart = witnessToStartToken.get(w);
        Integer witnessEnd = witnessToEndToken.get(w);
        List<Block.Instance> result = new ArrayList<>();
        for (int i= witnessStart; i < witnessEnd; i++) {
            List<Block.Instance> instance = block_array.get(i);
            if (instance != null) {
                result.addAll(instance);
            }
        }
        return result;
    }

    public int size() {
        return token_array.size();
    }
}
