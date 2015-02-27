package eu.interedition.collatex.suffixarray;

/**
 * In the "dense" scenario we keep "forward" mapping between original keys (shifted to
 * positive indexes) and their new key values. A "reverse" mapping is used to restore
 * original values in place of the mapped keys upon exit.
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
final class DensePositiveMapper implements ISymbolMapper {
    private final int offset;
    private final int[] forward;
    private final int[] backward;

    /*
     *
     */
    public DensePositiveMapper(int[] input, int start, int length) {
        final MinMax minmax = Tools.minmax(input, start, length);
        final int min = minmax.min;
        final int max = minmax.max;

        final int[] forward = new int[max - min + 1];
        final int offset = -min;

        // Mark all symbols present in the alphabet.
        final int end = start + length;
        for (int i = start; i < end; i++) {
            forward[input[i] + offset] = 1;
        }

        // Collect present symbols, assign unique codes.
        int k = 1;
        for (int i = 0; i < forward.length; i++) {
            if (forward[i] != 0) {
                forward[i] = k++;
            }
        }

        final int[] backward = new int[k];
        for (int i = start; i < end; i++) {
            final int v = forward[input[i] + offset];
            backward[v] = input[i];
        }

        this.offset = offset;
        this.forward = forward;
        this.backward = backward;
    }

    /*
     *
     */
    @Override
    public void map(int[] input, final int start, final int length) {
        for (int i = start, l = length; l > 0; l--, i++) {
            input[i] = forward[input[i] + offset];
        }
    }

    /*
     *
     */
    @Override
    public void undo(int[] input, final int start, final int length) {
        for (int i = start, l = length; l > 0; l--, i++) {
            input[i] = backward[input[i]];
        }
    }
}
