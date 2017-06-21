package eu.interedition.collatex.suffixarray;

import java.util.Arrays;

/**
 * <p>
 * Straightforward reimplementation of the recursive algorithm given in: <tt>
 * J. Kärkkäinen and P. Sanders. Simple linear work suffix array construction.
 * In Proc. 13th International Conference on Automata, Languages and Programming,
 * Springer, 2003
 * </tt>
 * <p>
 * This implementation is basically a translation of the C++ version given by Juha
 * Kärkkäinen and Peter Sanders.
 * <p>
 * The implementation of this algorithm makes some assumptions about the input. See
 * {@link #buildSuffixArray(int[], int, int)} for details.
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
public final class Skew implements ISuffixArrayBuilder {
    /**
     * Lexicographic order for pairs.
     */
    private static boolean leq(int a1, int a2, int b1, int b2) {
        return (a1 < b1 || (a1 == b1 && a2 <= b2));
    }

    /**
     * Lexicographic order for triples.
     */
    private static boolean leq(int a1, int a2, int a3, int b1, int b2, int b3) {
        return (a1 < b1 || (a1 == b1 && leq(a2, a3, b2, b3)));
    }

    /**
     * Stably sort indexes from src[0..n-1] to dst[0..n-1] with values in 0..K from v. A
     * constant offset of <code>vi</code> is added to indexes from src.
     */
    private static void radixPass(int[] src, int[] dst, int[] v, int vi,
                                  final int n, final int K, int start, int[] cnt) {
        // check counter array's size.
        assert cnt.length >= K + 1;
        Arrays.fill(cnt, 0, K + 1, 0);

        // count occurrences
        for (int i = 0; i < n; i++)
            cnt[v[start + vi + src[i]]]++;

        // exclusive prefix sums
        for (int i = 0, sum = 0; i <= K; i++) {
            final int t = cnt[i];
            cnt[i] = sum;
            sum += t;
        }

        // sort
        for (int i = 0; i < n; i++)
            dst[cnt[v[start + vi + src[i]]]++] = src[i];
    }

    /**
     * Find the suffix array SA of s[0..n-1] in {1..K}^n. require s[n] = s[n+1] = s[n+2] =
     * 0, n >= 2.
     */
    static int[] suffixArray(int[] s, int[] SA, int n, final int K, int start, int[] cnt) {
        final int n0 = (n + 2) / 3, n1 = (n + 1) / 3, n2 = n / 3, n02 = n0 + n2;

        final int[] s12 = new int[n02 + 3];
        s12[n02] = s12[n02 + 1] = s12[n02 + 2] = 0;
        final int[] SA12 = new int[n02 + 3];
        SA12[n02] = SA12[n02 + 1] = SA12[n02 + 2] = 0;
        final int[] s0 = new int[n0];
        final int[] SA0 = new int[n0];

        /*
         * generate positions of mod 1 and mod 2 suffixes the "+(n0-n1)" adds a dummy mod
         * 1 suffix if n%3 == 1
         */
        for (int i = 0, j = 0; i < n + (n0 - n1); i++)
            if ((i % 3) != 0) s12[j++] = i;

        // lsb radix sort the mod 1 and mod 2 triples
        cnt = ensureSize(cnt, K + 1);
        radixPass(s12, SA12, s, +2, n02, K, start, cnt);
        radixPass(SA12, s12, s, +1, n02, K, start, cnt);
        radixPass(s12, SA12, s, +0, n02, K, start, cnt);

        // find lexicographic names of triples
        int name = 0, c0 = -1, c1 = -1, c2 = -1;
        for (int i = 0; i < n02; i++) {
            if (s[start + SA12[i]] != c0 || s[start + SA12[i] + 1] != c1
                || s[start + SA12[i] + 2] != c2) {
                name++;
                c0 = s[start + SA12[i]];
                c1 = s[start + SA12[i] + 1];
                c2 = s[start + SA12[i] + 2];
            }

            if ((SA12[i] % 3) == 1) {
                // left half
                s12[SA12[i] / 3] = name;
            } else {
                // right half
                s12[SA12[i] / 3 + n0] = name;
            }
        }

        // recurse if names are not yet unique
        if (name < n02) {
            cnt = suffixArray(s12, SA12, n02, name, start, cnt);
            // store unique names in s12 using the suffix array
            for (int i = 0; i < n02; i++)
                s12[SA12[i]] = i + 1;
        } else {
            // generate the suffix array of s12 directly
            for (int i = 0; i < n02; i++)
                SA12[s12[i] - 1] = i;
        }

        // stably sort the mod 0 suffixes from SA12 by their first character
        for (int i = 0, j = 0; i < n02; i++)
            if (SA12[i] < n0) s0[j++] = 3 * SA12[i];
        radixPass(s0, SA0, s, 0, n0, K, start, cnt);

        // merge sorted SA0 suffixes and sorted SA12 suffixes
        for (int p = 0, t = n0 - n1, k = 0; k < n; k++) {
            // pos of current offset 12 suffix
            final int i = (SA12[t] < n0 ? SA12[t] * 3 + 1 : (SA12[t] - n0) * 3 + 2);
            // pos of current offset 0 suffix
            final int j = SA0[p];

            if (SA12[t] < n0 ? leq(s[start + i], s12[SA12[t] + n0], s[start + j],
                s12[j / 3]) : leq(s[start + i], s[start + i + 1], s12[SA12[t] - n0 + 1],
                s[start + j], s[start + j + 1], s12[j / 3 + n0])) {
                // suffix from SA12 is smaller
                SA[k] = i;
                t++;
                if (t == n02) {
                    // done --- only SA0 suffixes left
                    for (k++; p < n0; p++, k++)
                        SA[k] = SA0[p];
                }
            } else {
                SA[k] = j;
                p++;
                if (p == n0) {
                    // done --- only SA12 suffixes left
                    for (k++; t < n02; t++, k++) {
                        SA[k] = (SA12[t] < n0 ? SA12[t] * 3 + 1 : (SA12[t] - n0) * 3 + 2);
                    }
                }
            }
        }

        return cnt;
    }

    /**
     * Ensure array is large enough or reallocate (no copying).
     */
    private static int[] ensureSize(int[] tab, int length) {
        if (tab.length < length) {
            tab = null;
            tab = new int[length];
        }

        return tab;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additional constraints enforced by Karkkainen-Sanders algorithm:
     * <ul>
     * <li>non-negative (&gt;0) symbols in the input (because of radix sort)</li>
     * <li><code>input.length</code> &gt;= <code>start + length + 3</code> (to simplify
     * border cases)</li>
     * <li>length &gt;= 2</li>
     * </ul>
     * <p>
     * If the input contains zero or negative values, or has no extra trailing cells,
     * adapters can be used in the following way:
     * <p>
     * <pre>
     * return new {@link DensePositiveDecorator}(
     *      new {@link ExtraTrailingCellsDecorator}(
     *          new {@link Skew}(), 3));
     * </pre>
     *
     * @see ExtraTrailingCellsDecorator
     * @see DensePositiveDecorator
     */
    @Override
    public int[] buildSuffixArray(int[] input, int start, int length) {
        Tools.assertAlways(input != null, "input must not be null");
        Tools.assertAlways(length >= 2, "input length must be >= 2");
        Tools.assertAlways(input.length >= start + length + 3, "no extra space after input end");
        assert Tools.allPositive(input, start, length);

        final int alphabetSize = Tools.max(input, start, length);
        final int[] SA = new int[length + 3];

        // Preserve the tail of the input (destroyed when constructing the array).
        final int[] tail = new int[3];
        System.arraycopy(input, start + length, tail, 0, 3);
        Arrays.fill(input, start + length, start + length + 3, 0);

        suffixArray(input, SA, length, alphabetSize, start, new int[alphabetSize + 2]);

        // Reconstruct the input's tail.
        System.arraycopy(tail, 0, input, start + length, 3);
        return SA;
    }
}
