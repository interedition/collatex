package eu.interedition.collatex.dekker.token_index;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;
import eu.interedition.collatex.util.StreamUtil;

import java.util.*;

/**
 * Created by ronald on 4/20/15.
 */
public class TokenIndex {
    private final List<? extends Iterable<Token>> witnesses;
    private final Comparator<Token> comparator;
    //TODO: not sure this functionality should be in this class or in a separate class
    private Map<Witness, Integer> witnessToStartToken;
    private Map<Witness, Integer> witnessToEndToken;
    public Token[] token_array;
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
        this.token_array = this.prepareTokenArray();
        SuffixData suffixData = SuffixArrays.createWithLCP(token_array, new SAIS(), comparator);
        this.suffix_array = suffixData.getSuffixArray();
        this.LCP_array = suffixData.getLCP();
        this.blocks = splitLCP_ArrayIntoIntervals();
        constructWitnessToBlockInstancesMap();
    }

    private Token[] prepareTokenArray() {
        List<Token> tempTokenList = new ArrayList<>();
        int counter = 0;
        witnessToStartToken = new HashMap<>();
        witnessToEndToken = new HashMap<>();
        for (Iterable<Token> tokens : witnesses) {
            final Witness witness = StreamUtil.stream(tokens)
                    .findFirst()
                    .map(Token::getWitness)
                    .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

            witnessToStartToken.put(witness, counter);
            for (Token t : tokens) {
                tempTokenList.add(t);
                counter++;
            }
            witnessToEndToken.put(witness, counter);
            tempTokenList.add(new MarkerToken(witnessToStartToken.size()));
            counter++;
        }
        return tempTokenList.toArray(new Token[tempTokenList.size()]);
    }

    protected static class MarkerToken implements Token {
        private final int witnessIdentifier;

        public MarkerToken(int size) {
            this.witnessIdentifier = size;
        }

        @Override
        public String toString() {
            return "$" + witnessIdentifier;
        }

        @Override
        public Witness getWitness() {
            throw new RuntimeException("A marker token is not part of any witness! The call to this method should never have happened!");
        }
    }

    static class MarkerTokenComparator implements Comparator<Token> {
        private Comparator<Token> delegate;

        public MarkerTokenComparator(Comparator<Token> delegate) {
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
