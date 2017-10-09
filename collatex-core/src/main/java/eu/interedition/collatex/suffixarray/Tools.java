package eu.interedition.collatex.suffixarray;

/**
 * Utility methods used throughout entire project.
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
final class Tools {
    private Tools() {
        // No instances.
    }

    /**
     * Check if all symbols in the given range are greater than 0, return
     * <code>true</code> if so, <code>false</code> otherwise.
     */
    static boolean allPositive(int[] input, int start, int length) {
        for (int i = length - 1, index = start; i >= 0; i--, index++) {
            if (input[index] <= 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determine the maximum value in a slice of an array.
     */
    static int max(int[] input, int start, int length) {
        assert length >= 1;

        int max = input[start];
        for (int i = length - 2, index = start + 1; i >= 0; i--, index++) {
            final int v = input[index];
            if (v > max) {
                max = v;
            }
        }

        return max;
    }

    /**
     * Determine the minimum value in a slice of an array.
     */
    static int min(int[] input, int start, int length) {
        assert length >= 1;

        int min = input[start];
        for (int i = length - 2, index = start + 1; i >= 0; i--, index++) {
            final int v = input[index];
            if (v < min) {
                min = v;
            }
        }

        return min;
    }

    /**
     * Calculate minimum and maximum value for a slice of an array.
     */
    static MinMax minmax(int[] input, final int start, final int length) {
        int max = input[start];
        int min = max;
        for (int i = length - 2, index = start + 1; i >= 0; i--, index++) {
            final int v = input[index];
            if (v > max) {
                max = v;
            }
            if (v < min) {
                min = v;
            }
        }

        return new MinMax(min, max);
    }

    /**
     * Throw {@link AssertionError} if a condition is <code>false</code>. This should
     * be called when the assertion must be always verified (as in the case of verifying
     * the algorithm's preconditions). For other, internal assertions, one should use
     * <code>assert</code> keyword so that such assertions can be disabled at run-time (for
     * performance reasons).
     */
    static void assertAlways(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError(msg);
        }
    }
}
