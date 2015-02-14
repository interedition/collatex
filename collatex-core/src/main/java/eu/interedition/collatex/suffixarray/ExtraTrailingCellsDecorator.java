package eu.interedition.collatex.suffixarray;

/**
 * A decorator around {@link ISuffixArrayBuilder} that:
 * <ul>
 * <li>provides extra space after the input for end-of-string markers</li>
 * <li>shifts the input to zero-based positions.</li>
 * </ul>
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
public final class ExtraTrailingCellsDecorator implements ISuffixArrayBuilder {
    private final ISuffixArrayBuilder delegate;
    private final int extraCells;

    /**
     * @see SuffixArrays#MAX_EXTRA_TRAILING_SPACE
     */
    public ExtraTrailingCellsDecorator(ISuffixArrayBuilder delegate, int extraCells) {
        this.delegate = delegate;
        this.extraCells = extraCells;
    }

    /*
     *
     */
    @Override
    public int[] buildSuffixArray(int[] input, final int start, final int length) {
        if (start == 0 && start + length + extraCells < input.length) {
            return delegate.buildSuffixArray(input, start, length);
        }

        final int[] shifted = new int[input.length + extraCells];
        System.arraycopy(input, start, shifted, 0, length);

        final int[] SA = delegate.buildSuffixArray(shifted, 0, length);

        return SA;
    }
}
