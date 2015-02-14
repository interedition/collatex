package eu.interedition.collatex.suffixarray;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * An adapter for constructing suffix arrays on generic arrays.
 *
 * @author Anton Olsson <abc2386@gmail.com> for friprogramvarusyndikatet.se
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
        this.input = new int[length + SuffixArrays.MAX_EXTRA_TRAILING_SPACE];

        //System.out.println("Renaming tokens ...");
        /*
         * Here we create a mapping for the token to an integer id which we
         * can use in the suffax array construction algorithm.
         */
        this.tokIDs = new TreeMap<T, Integer>(comparator);

        // put and order all tokens in tokIDs
        for (int i = 0; i < length; i++) {
            tokIDs.put(tokens[i], null); // null is temporary placeholder value
        }

        // assign each token an ascending id
        int _id = 1;
        for (Entry<T, Integer> entry : tokIDs.entrySet()) {
            entry.setValue(_id++);
        }

        // fill input array with ids
        for (int i = 0; i < length; i++) {
            input[i] = tokIDs.get(tokens[i]);
        }

        //System.out.println("Renaming tokens done.");

        return delegate.buildSuffixArray(input, 0, length);
    }
}

