package eu.interedition.collatex.suffixarray;

import java.util.Comparator;
import java.util.TreeMap;

/**
 * An adapter for constructing suffix arrays on generic arrays.
 *
 * @author <a href="mailto:abc2386@gmail.com">Anton Olsson</a> for friprogramvarusyndikatet.se
 */
class GenericArrayAdapter<T> {

    private final ISuffixArrayBuilder delegate;
    int[] input;
    TreeMap<T, Integer> tokIDs;
    private final Comparator<? super T> comparator;

    public GenericArrayAdapter(ISuffixArrayBuilder builder) {
        // TODO make sure T is comparable
        this.delegate = builder;
        this.comparator = null;
    }

    public GenericArrayAdapter(ISuffixArrayBuilder builder, Comparator<? super T> comparator) {
        // TODO make sure that comparator != null or T is comparable
        this.delegate = builder;
        this.comparator = comparator;
    }

    /**
     * Construct a suffix array for a given generic token array.
     */
    public int[] buildSuffixArray(T[] tokens) {
        final int length = tokens.length;
        /*
         * Allocate slightly more space, some suffix construction strategies need it and
         * we don't want to waste space for multiple symbol mappings.
         */
        input = new int[length + SuffixArrays.MAX_EXTRA_TRAILING_SPACE];

        //System.out.println("Assigning token ids ...");

        /*
         * We associate every token to an id, all `equal´ tokens to the same id.
         * The suffix array is built using only the the ids.
         */
        tokIDs = new TreeMap<>(comparator);

        for (int i = 0; i < length; i++) {
            tokIDs.putIfAbsent(tokens[i], i);
            input[i] = tokIDs.get(tokens[i]);
        }

        //System.out.println("Token ids assigned.");

        return delegate.buildSuffixArray(input, 0, length);
    }
}
