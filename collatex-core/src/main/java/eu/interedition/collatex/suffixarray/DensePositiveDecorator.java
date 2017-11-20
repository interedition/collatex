package eu.interedition.collatex.suffixarray;

/**
 * A decorator around {@link ISuffixArrayBuilder} that accepts any input symbols and maps
 * it to non-negative, compact (dense) alphabet. Relative symbols order is preserved (changes are
 * limited to a constant shift and compaction of symbols). The input is remapped in-place,
 * but additional space is required for the mapping.
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
public final class DensePositiveDecorator implements ISuffixArrayBuilder {
    private final ISuffixArrayBuilder delegate;

    /*
     *
     */
    public DensePositiveDecorator(ISuffixArrayBuilder delegate) {
        this.delegate = delegate;
    }

    /*
     *
     */
    @Override
    public int[] buildSuffixArray(int[] input, final int start, final int length) {
        final MinMax minmax = Tools.minmax(input, start, length);

        final ISymbolMapper mapper;
        if (minmax.range() > 0x10000) {
            throw new RuntimeException("Large symbol space not implemented yet.");
        }
        mapper = new DensePositiveMapper(input, start, length);

        mapper.map(input, start, length);
        try {
            return delegate.buildSuffixArray(input, start, length);
        } finally {
            mapper.undo(input, start, length);
        }
    }
}
