package eu.interedition.collatex.suffixarray;

import java.util.Arrays;

/**
 * <p>
 * Straightforward reimplementation of deep-shallow algorithm given in: <tt>
 * Giovanni Manzini and Paolo Ferragina. Engineering a lightweight suffix array construction algorithm.
 * </tt>
 * <p>
 * This implementation is basically a translation of the C version given by Giovanni Manzini
 * <p>
 * The implementation of this algorithm makes some assumptions about the input. See
 * {@link #buildSuffixArray(int[], int, int)} for details.
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
public class DeepShallow implements ISuffixArrayBuilder {
    private static class SplitGroupResult {
        final int equal;
        final int lower;

        public SplitGroupResult(int equal, int lower) {
            this.equal = equal;
            this.lower = lower;
        }
    }

    private static class Node {
        int skip;
        int key;
        Node right;
        // original author uses down as a pointer to another Node, but sometimes he stores
        // int values in it. Because of that, we have two following variables (luckily we
        // could do so :)).
        Node down;
        int downInt;
    }

    /**
     * TODO: What is this magic constant? Do not make it public and do not reuse it anywhere where it isn't needed
     * (especially not in the tests). If this algorithm has special considerations, we can run algorithm-specific tests
     * with an appropriate decorator.
     */
    final static int OVERSHOOT = 575;
    private final static int SETMASK = 1 << 30;
    private final static int CLEARMASK = ~SETMASK;
    private final static int MARKER = 1 << 31;

    /**
     * recursion limit for mk quicksort:
     */
    private final static int MK_QS_TRESH = 20;

    private final static int MAX_TRESH = 30;

    /**
     * limit for shallow_sort
     */
    private final static int SHALLOW_LIMIT = 550;

    /**
     * maximum offset considered when searching a pseudo anchor
     */
    private final static int MAX_PSEUDO_ANCHOR_OFFSET = 0;

    /**
     * maximum ratio bucket_size/group_size accepted for pseudo anchor_sorting
     */
    private final static int B2G_RATIO = 1000;

    /**
     * Update anchor ranks when determining rank for pseudo-sorting
     */
    private final static boolean UPDATE_ANCHOR_RANKS = false;

    /**
     * blind sort is used for groups of size &le; Text_size/Blind_sort_ratio
     */
    private final static int BLIND_SORT_RATIO = 2000;

    private final static int STACK_SIZE = 100;

    private int[] text;
    private int textSize;
    private int[] suffixArray;
    private int anchorDist; // distance between anchors
    private int anchorNum;
    private int[] anchorOffset;
    private int[] anchorRank;
    private final int[] ftab = new int[66049];
    private final int[] bucketRanked = new int[66049];
    private final int[] runningOrder = new int[257];
    private final int[] lcpAux = new int[1 + MAX_TRESH];
    private int lcp;
    private int cmpLeft;
    private int cmpDone;
    private int aux;
    private int auxWritten;
    private int stackSize;
    private Node[] stack;
    private int start;

    /**
     * If <code>true</code>, {@link #buildSuffixArray(int[], int, int)} uses a copy of the input so it is left intact.
     */
    private final boolean preserveInput;

    public DeepShallow() {
        preserveInput = true;
    }

    public DeepShallow(boolean preserveInput) {
        this.preserveInput = preserveInput;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additional constraints enforced by Deep-Shallow algorithm:
     * <ul>
     * <li>non-negative (&ge;0) symbols in the input</li>
     * <li>maximal symbol value &lt; <code>256</code></li>
     * <li><code>input.length</code> &ge; <code>start + length</code> if {@link #preserveInput} is <tt>true</tt></li>
     * <li><code>input.length</code> &ge; <code>start + length + {@link #OVERSHOOT}</code> if {@link #preserveInput} is <tt>false</tt></li>
     * <li>length &ge; 2</li>
     * </ul>
     */
    @Override
    public int[] buildSuffixArray(int[] input, int start, int length) {
        Tools.assertAlways(input.length >= start + length, "Input array is too short");

        MinMax mm = Tools.minmax(input, start, length);
        Tools.assertAlways(mm.min >= 0, "input must not be negative");
        Tools.assertAlways(mm.max < 256, "max alphabet size is 256");

        lcp = 1;
        stack = new Node[length];
        this.start = start;
        if (preserveInput) {
            this.start = 0;
            text = new int[length + OVERSHOOT];
            System.arraycopy(input, start, text, 0, length);
        } else {
            Tools.assertAlways(input.length >= start + length + OVERSHOOT, "Input array length must have a trailing space of at least " + OVERSHOOT + " bytes.");
            text = input;
        }

        for (int i = length; i < length + OVERSHOOT; i++) {
            text[this.start + i] = 0;
        }

        textSize = length;
        suffixArray = new int[length];

        int i, j, ss, sb, k, c1, c2, numQSorted = 0;
        boolean[] bigDone = new boolean[257];
        int[] copyStart = new int[257];
        int[] copyEnd = new int[257];

        // ------ init array containing positions of anchors
        if (anchorDist == 0) {
            anchorNum = 0;
        } else {
            anchorNum = 2 + (length - 1) / anchorDist; // see comment for helped_sort()
            anchorRank = new int[anchorNum];
            anchorOffset = new int[anchorNum];
            for (i = 0; i < anchorNum; i++) {
                anchorRank[i] = -1; // pos of anchors is initially unknown
                anchorOffset[i] = anchorDist; // maximum possible value
            }
        }

        // ---------- init ftab ------------------
        // at first, clear values in ftab
        for (i = 0; i < 66049; i++)
            ftab[i] = 0;

        c1 = text[this.start];
        for (i = 1; i <= textSize; i++) {
            c2 = text[this.start + i];
            ftab[(c1 << 8) + c2]++;
            c1 = c2;
        }
        for (i = 1; i < 66049; i++)
            ftab[i] += ftab[i - 1];

        // -------- sort suffixes considering only the first two chars
        c1 = text[this.start];
        for (i = 0; i < textSize; i++) {
            c2 = text[this.start + i + 1];
            j = (c1 << 8) + c2;
            c1 = c2;
            ftab[j]--;
            suffixArray[ftab[j]] = i;
        }

        /* decide on the running order */
        calculateRunningOrder();
        for (i = 0; i < 257; i++) {
            bigDone[i] = false;
        }

        /* Really do the suffix sorting */
        for (i = 0; i <= 256; i++) {

            /*--
              Process big buckets, starting with the least full.
              --*/
            ss = runningOrder[i];
            /*--
            Complete the big bucket [ss] by sorting
            any unsorted small buckets [ss, j].  Hopefully
            previous pointer-scanning phases have already
            completed many of the small buckets [ss, j], so
            we don't have to sort them at all.
            --*/
            for (j = 0; j <= 256; j++) {
                if (j != ss) {
                    sb = (ss << 8) + j;
                    if ((ftab[sb] & SETMASK) == 0) {
                        int lo = ftab[sb] & CLEARMASK;
                        int hi = (ftab[sb + 1] & CLEARMASK) - 1;
                        if (hi > lo) {
                            shallowSort(lo, hi - lo + 1);
                            numQSorted += (hi - lo + 1);
                        }
                    }
                    ftab[sb] |= SETMASK;
                }
            }
            {
                for (j = 0; j <= 256; j++) {
                    copyStart[j] = ftab[(j << 8) + ss] & CLEARMASK;
                    copyEnd[j] = (ftab[(j << 8) + ss + 1] & CLEARMASK) - 1;
                }
                // take care of the virtual -1 char in position textSize+1
                if (ss == 0) {
                    k = textSize - 1;
                    c1 = text[this.start + k];
                    if (!bigDone[c1])
                        suffixArray[copyStart[c1]++] = k;
                }
                for (j = ftab[ss << 8] & CLEARMASK; j < copyStart[ss]; j++) {
                    k = suffixArray[j] - 1;
                    if (k < 0)
                        continue;
                    c1 = text[this.start + k];
                    if (!bigDone[c1])
                        suffixArray[copyStart[c1]++] = k;
                }
                for (j = (ftab[(ss + 1) << 8] & CLEARMASK) - 1; j > copyEnd[ss]; j--) {
                    k = suffixArray[j] - 1;
                    if (k < 0)
                        continue;
                    c1 = text[this.start + k];
                    if (!bigDone[c1])
                        suffixArray[copyEnd[c1]--] = k;
                }
            }
            for (j = 0; j <= 256; j++)
                ftab[(j << 8) + ss] |= SETMASK;
            bigDone[ss] = true;
        } // endfor

        return suffixArray;
    }

    /**
     * This is the multikey quicksort from bentley-sedgewick modified so that it stops recursion when depth reaches
     * {@link #SHALLOW_LIMIT} (that is when two or more suffixes have {@link #SHALLOW_LIMIT} chars in common).
     */
    private void shallowSort(int a, int n) {
        // call multikey quicksort
        // skip 2 chars since suffixes come from the same bucket
        shallowMkq32(a, n, 2);

    }

    /**
     * recursive multikey quicksort from Bentley-Sedgewick.
     * <p>
     * Stops when text_depth reaches {@link #SHALLOW_LIMIT} that is when we have found that the current set of strings
     * have {@link #SHALLOW_LIMIT} chars in common
     */
    private void shallowMkq32(int a, int n, int text_depth) {

        int partval, val;
        int pa = 0, pb = 0, pc = 0, pd = 0, pl = 0, pm = 0, pn = 0;// pointers
        int d, r;
        int next_depth;// text pointer
        boolean repeatFlag = true;

        // ---- On small arrays use insertions sort
        if (n < MK_QS_TRESH) {
            shallowInssortLcp(a, n, text_depth);
            return;
        }

        // ----------- choose pivot --------------
        while (repeatFlag) {

            repeatFlag = false;
            pl = a;
            pm = a + (n / 2);
            pn = a + (n - 1);
            if (n > 30) { // On big arrays, pseudomedian of 9
                d = (n / 8);
                pl = med3(pl, pl + d, pl + 2 * d, text_depth);
                pm = med3(pm - d, pm, pm + d, text_depth);
                pn = med3(pn - 2 * d, pn - d, pn, text_depth);
            }
            pm = med3(pl, pm, pn, text_depth);
            swap2(a, pm);
            partval = ptr2char32(a, text_depth);
            pa = pb = a + 1;
            pc = pd = a + n - 1;
            // -------- partition -----------------
            for (;;) {
                while (pb <= pc && (val = ptr2char32(pb, text_depth)) <= partval) {
                    if (val == partval) {
                        swap2(pa, pb);
                        pa++;
                    }
                    pb++;
                }
                while (pb <= pc && (val = ptr2char32(pc, text_depth)) >= partval) {
                    if (val == partval) {
                        swap2(pc, pd);
                        pd--;
                    }
                    pc--;
                }
                if (pb > pc)
                    break;
                swap2(pb, pc);
                pb++;
                pc--;
            }
            if (pa > pd) {
                // all values were equal to partval: make it simpler
                if ((next_depth = text_depth + 4) >= SHALLOW_LIMIT) {
                    helpedSort(a, n, next_depth);
                    return;
                }
                text_depth = next_depth;
                repeatFlag = true;

            }

        }
        // partition a[] into the values smaller, equal, and larger that partval
        pn = a + n;
        r = min(pa - a, pb - pa);
        vecswap2(a, pb - r, r);
        r = min(pd - pc, pn - pd - 1);
        vecswap2(pb, pn - r, r);
        // --- sort smaller strings -------
        if ((r = pb - pa) > 1)
            shallowMkq32(a, r, text_depth);
        // --- sort strings starting with partval -----
        if ((next_depth = text_depth + 4) < SHALLOW_LIMIT)
            shallowMkq32(a + r, pa - pd + n - 1, next_depth);
        else
            helpedSort(a + r, pa - pd + n - 1, next_depth);
        if ((r = pd - pc) > 1)
            shallowMkq32(a + n - r, r, text_depth);

    }

    private void vecswap2(int a, int b, int n) {
        while (n-- > 0) {
            int t = suffixArray[a];
            suffixArray[a++] = suffixArray[b];
            suffixArray[b++] = t;
        }
    }

    private static int min(int i, int j) {
        return i < j ? i : j;
    }

    /**
     * this is the insertion sort routine called by multikey-quicksort for sorting small groups. During insertion sort
     * the comparisons are done calling cmp_unrolled_shallow_lcp() and two strings are equal if the coincides for
     * SHALLOW_LIMIT characters. After this first phase we sort groups of "equal_string" using helped_sort().
     * <p>
     */
    private void shallowInssortLcp(int a, int n, int text_depth) {
        int i, j, j1, lcp_new, r, ai, lcpi;
        int cmp_from_limit;
        int text_depth_ai;// pointer
        // --------- initialize ----------------

        lcpAux[0] = -1; // set lcp[-1] = -1
        for (i = 0; i < n; i++) {
            lcpAux[lcp + i] = 0;
        }
        cmp_from_limit = SHALLOW_LIMIT - text_depth;

        // ----- start insertion sort -----------
        for (i = 1; i < n; i++) {
            ai = suffixArray[a + i];
            lcpi = 0;
            text_depth_ai = ai + text_depth;
            j = i;
            j1 = j - 1; // j1 is a shorhand for j-1
            while (true) {

                // ------ compare ai with a[j-1] --------
                cmpLeft = cmp_from_limit - lcpi;
                r = cmpUnrolledShallowLcp(lcpi + suffixArray[a + j1] + text_depth, lcpi + text_depth_ai);
                lcp_new = cmp_from_limit - cmpLeft; // lcp between ai and a[j1]
                assert (r != 0 || lcp_new >= cmp_from_limit);

                if (r <= 0) { // we have a[j-1] <= ai
                    lcpAux[lcp + j1] = lcp_new; // ai will be written in a[j]; update
                    // lcp[j-1]
                    break;
                }

                // --- we have a[j-1]>ai. a[j-1] and maybe other will be moved down
                // --- use lcp to move down as many elements of a[] as possible
                lcpi = lcp_new;
                do {
                    suffixArray[a + j] = suffixArray[a + j1]; // move down a[j-1]
                    lcpAux[lcp + j] = lcpAux[lcp + j1]; // move down lcp[j-1]
                    j = j1;
                    j1--; // update j and j1=j-1
                } while (lcpi < lcpAux[lcp + j1]); // recall that lcp[-1]=-1

                if (lcpi > lcpAux[lcp + j1])
                    break; // ai will be written in position j

                // if we get here lcpi==lcp[j1]: we will compare them at next iteration

            } // end for(j=i ...
            suffixArray[a + j] = ai;
            lcpAux[lcp + j] = lcpi;
        } // end for(i=1 ...
          // ----- done with insertion sort. now sort groups of equal strings
        for (i = 0; i < n - 1; i = j + 1) {
            for (j = i; j < n; j++)
                if (lcpAux[lcp + j] < cmp_from_limit)
                    break;
            if (j - i > 0)
                helpedSort(a + i, j - i + 1, SHALLOW_LIMIT);
        }
    }

    /**
     * Function to compare two strings originating from the *b1 and *b2 The size of the unrolled loop must be at most
     * equal to the costant CMP_OVERSHOOT defined in common.h When the function is called cmpLeft must contain the
     * maximum number of comparisons the algorithm can do before returning 0 (equal strings) At exit cmpLeft has been
     * decreased by the # of comparisons done
     */
    private int cmpUnrolledShallowLcp(int b1, int b2) {

        int c1, c2;

        // execute blocks of 16 comparisons until a difference
        // is found or we run out of the string
        do {
            // 1
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                return c1 - c2;
            }
            b1++;
            b2++;
            // 2
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 1;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 3
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 2;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 4
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 3;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 5
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 4;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 6
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 5;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 7
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 6;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 8
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 7;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 9
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 8;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 10
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 9;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 11
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 10;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 12
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 11;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 13
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 12;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 14
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 13;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 15
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 14;
                return c1 - c2;
            }
            b1++;
            b2++;
            // 16
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpLeft -= 15;
                return c1 - c2;
            }
            b1++;
            b2++;
            // if we have done enough comparisons the strings are considered equal
            cmpLeft -= 16;
            if (cmpLeft <= 0)
                return 0;
            // assert( b1<Upper_text_limit && b2<Upper_text_limit);
        } while (true);
        // return b2 - b1;
    }

    /**
     * This procedure sort the strings a[0] ... a[n-1] with the help of an anchor. The real sorting is done by the
     * procedure anchor_sort(). Here we choose the anchor. The parameter depth is the number of chars that a[0] ...
     * a[n-1] are known to have in common (thus a direct comparison among a[i] and a[j] should start from position
     * depth) Note that a[] is a subsection of the sa therefore a[0] ... a[n-1] are starting position of suffixes For
     * every a[i] we look at the anchor a[i]/anchorDist and the one after that. This justifies the definition of
     * anchorNum (the size of Anchor_ofset[] and anchorRank[] defined in ds_sort()) as anchorNum = 2 + (n-1)/anchorDist
     */
    private void helpedSort(int a, int n, int depth) {
        int i, curr_sb, diff, toffset, aoffset;
        int text_pos, anchor_pos, anchor, anchor_rank;
        int min_forw_offset, min_forw_offset_buc, max_back_offset;
        int best_forw_anchor, best_forw_anchor_buc, best_back_anchor;
        int forw_anchor_index, forw_anchor_index_buc, back_anchor_index;

        if (n == 1)
            return; // simplest case: only one string

        // if there are no anchors use pseudo-anchors or deep_sort
        if (anchorDist == 0) {
            pseudoOrDeepSort(a, n, depth);
            return;
        }

        // compute the current bucket
        curr_sb = getSmallBucket(suffixArray[a]);

        // init best anchor variables with illegal values
        min_forw_offset = min_forw_offset_buc = Integer.MAX_VALUE;
        max_back_offset = Integer.MIN_VALUE;
        best_forw_anchor = best_forw_anchor_buc = best_back_anchor = -1;
        forw_anchor_index = forw_anchor_index_buc = back_anchor_index = -1;

        // look at the anchor preceeding each a[i]
        for (i = 0; i < n; i++) {
            text_pos = suffixArray[a + i];
            // get anchor preceeding text_pos=a[i]
            anchor = text_pos / anchorDist;
            toffset = text_pos % anchorDist; // distance of a[i] from anchor
            aoffset = anchorOffset[anchor]; // distance of sorted suf from anchor
            if (aoffset < anchorDist) { // check if it is a "sorted" anchor
                diff = aoffset - toffset;
                if (diff > 0) { // anchor <= a[i] < (sorted suffix)
                    if (curr_sb != getSmallBucket(text_pos + diff)) {
                        if (diff < min_forw_offset) {
                            min_forw_offset = diff;
                            best_forw_anchor = anchor;
                            forw_anchor_index = i;
                        }
                    } else { // the sorted suffix belongs to the same bucket of a[0]..a[n-1]
                        if (diff < min_forw_offset_buc) {
                            min_forw_offset_buc = diff;
                            best_forw_anchor_buc = anchor;
                            forw_anchor_index_buc = i;
                        }
                    }
                } else { // diff<0 => anchor <= (sorted suffix) < a[i]
                    if (diff > max_back_offset) {
                        max_back_offset = diff;
                        best_back_anchor = anchor;
                        back_anchor_index = i;
                    }
                    // try to find a sorted suffix > a[i] by looking at next anchor
                    aoffset = anchorOffset[++anchor];
                    if (aoffset < anchorDist) {
                        diff = anchorDist + aoffset - toffset;
                        assert (diff > 0);
                        if (curr_sb != getSmallBucket(text_pos + diff)) {
                            if (diff < min_forw_offset) {
                                min_forw_offset = diff;
                                best_forw_anchor = anchor;
                                forw_anchor_index = i;
                            }
                        } else {
                            if (diff < min_forw_offset_buc) {
                                min_forw_offset_buc = diff;
                                best_forw_anchor_buc = anchor;
                                forw_anchor_index_buc = i;
                            }
                        }
                    }
                }
            }
        }

        // ------ if forward anchor_sort is possible, do it! --------
        if (best_forw_anchor >= 0 && min_forw_offset < depth - 1) {
            anchor_pos = suffixArray[a + forw_anchor_index] + min_forw_offset;
            anchor_rank = anchorRank[best_forw_anchor];
            generalAnchorSort(a, n, anchor_pos, anchor_rank, min_forw_offset);
            if (anchorDist > 0)
                updateAnchors(a, n);
            return;
        }

        boolean fail = false;
        if (best_back_anchor >= 0) {
            int T0, Ti;// text pointers
            int j;

            // make sure that the offset is legal for all a[i]
            for (i = 0; i < n; i++) {
                if (suffixArray[a + i] + max_back_offset < 0)
                    fail = true;
                // goto fail; // illegal offset, give up
            }
            // make sure that a[0] .. a[n-1] are preceded by the same substring
            T0 = suffixArray[a];
            for (i = 1; i < n; i++) {
                Ti = suffixArray[a + i];
                for (j = max_back_offset; j <= -1; j++)
                    if (text[this.start + T0 + j] != text[this.start + Ti + j])
                        fail = true;
                // goto fail; // mismatch, give up
            }
            if (!fail) {
                // backward anchor sorting is possible
                anchor_pos = suffixArray[a + back_anchor_index] + max_back_offset;
                anchor_rank = anchorRank[best_back_anchor];
                generalAnchorSort(a, n, anchor_pos, anchor_rank, max_back_offset);
                if (anchorDist > 0)
                    updateAnchors(a, n);
                return;
            }
        }
        if (fail) {
            if (best_forw_anchor_buc >= 0 && min_forw_offset_buc < depth - 1) {
                int equal = 0, lower = 0, upper = 0;

                anchor_pos = suffixArray[a + forw_anchor_index_buc] + min_forw_offset_buc;
                anchor_rank = anchorRank[best_forw_anchor_buc];

                // establish how many suffixes can be sorted using anchor_sort()
                SplitGroupResult res = splitGroup(a, n, depth, min_forw_offset_buc, forw_anchor_index_buc, lower);
                equal = res.equal;
                lower = res.lower;
                if (equal == n) {
                    generalAnchorSort(a, n, anchor_pos, anchor_rank, min_forw_offset_buc);
                } else {
                    // -- a[0] ... a[n-1] are split into 3 groups: lower, equal, upper
                    upper = n - equal - lower;
                    // printf("Warning! lo=%d eq=%d up=%d a=%x\n",lower,equal,upper,(int)a);
                    // sort the equal group
                    if (equal > 1)
                        generalAnchorSort(a + lower, equal, anchor_pos, anchor_rank, min_forw_offset_buc);

                    // sort upper and lower groups using deep_sort
                    if (lower > 1)
                        pseudoOrDeepSort(a, lower, depth);
                    if (upper > 1)
                        pseudoOrDeepSort(a + lower + equal, upper, depth);
                } // end if(equal==n) ... else
                if (anchorDist > 0)
                    updateAnchors(a, n);
                return;
            } // end hard case

        }
        // ---------------------------------------------------------------
        // If we get here it means that everything failed
        // In this case we simply deep_sort a[0] ... a[n-1]
        // ---------------------------------------------------------------
        pseudoOrDeepSort(a, n, depth);

    }

    /**
     * This function takes as input an array a[0] .. a[n-1] of suffixes which share the first "depth" chars. "pivot" in
     * an index in 0..n-1 and offset and integer>0. The function splits a[0] .. a[n-1] into 3 groups: first the suffixes
     * which are smaller than a[pivot], then those which are equal to a[pivot] and finally those which are greater than
     * a[pivot]. Here, smaller, equal, larger refer to a lexicographic ordering limited to the first depth+offest chars
     * (since the first depth chars are equal we only look at the chars in position depth, depth+1, ... depth+offset-1).
     * The function returns the number "num" of suffixes equal to a[pivot], and stores in *first the first of these
     * suffixes. So at the end the smaller suffixes are in a[0] ... a[first-1], the equal suffixes in a[first] ...
     * a[first+num-1], the larger suffixes in a[first+num] ... a[n-1] The splitting is done using a modified mkq()
     */
    private SplitGroupResult splitGroup(int a, int n, int depth, int offset, int pivot, int first) {
        int r, partval;
        int pa, pb, pc, pd, pa_old, pd_old;// pointers
        int pivot_pos;
        int text_depth, text_limit;// pointers

        // --------- initialization ------------------------------------
        pivot_pos = suffixArray[a + pivot]; // starting position in T[] of pivot
        text_depth = depth;
        text_limit = text_depth + offset;

        // -------------------------------------------------------------
        // In the following for() loop:
        // [pa ... pd] is the current working region,
        // pb moves from pa towards pd
        // pc moves from pd towards pa
        // -------------------------------------------------------------
        pa = a;
        pd = a + n - 1;

        for (; pa != pd && (text_depth < text_limit); text_depth++) {
            // ------ the pivot char is text[this.start + pivot_pos+depth] where
            // depth = text_depth-text. This is text_depth[pivot_pos]
            partval = text[this.start + text_depth + pivot_pos];
            // ----- partition ------------
            pb = pa_old = pa;
            pc = pd_old = pd;
            for (;;) {
                while (pb <= pc && (r = ptr2char(pb, text_depth) - partval) <= 0) {
                    if (r == 0) {
                        swap2(pa, pb);
                        pa++;
                    }
                    pb++;
                }
                while (pb <= pc && (r = ptr2char(pc, text_depth) - partval) >= 0) {
                    if (r == 0) {
                        swap2(pc, pd);
                        pd--;
                    }
                    pc--;
                }
                if (pb > pc)
                    break;
                swap2(pb, pc);
                pb++;
                pc--;
            }
            r = min(pa - pa_old, pb - pa);
            vecswap2(pa_old, pb - r, r);
            r = min(pd - pc, pd_old - pd);
            vecswap2(pb, pd_old + 1 - r, r);
            // ------ compute new boundaries -----
            pa = pa_old + (pb - pa); // there are pb-pa chars < partval
            pd = pd_old - (pd - pc); // there are pd-pc chars > partval

        }

        first = pa - a; // index in a[] of the first suf. equal to pivot
        // return pd-pa+1; // return number of suffixes equal to pivot
        return new SplitGroupResult(pd - pa + 1, first);

    }

    /**
     * given a SORTED array of suffixes a[0] .. a[n-1] updates anchorRank[] and anchorOffset[]
     */
    private void updateAnchors(int a, int n) {
        int i, anchor, toffset, aoffset, text_pos;

        for (i = 0; i < n; i++) {
            text_pos = suffixArray[a + i];
            // get anchor preceeding text_pos=a[i]
            anchor = text_pos / anchorDist;
            toffset = text_pos % anchorDist; // distance of a[i] from anchor
            aoffset = anchorOffset[anchor]; // dist of sorted suf from anchor
            if (toffset < aoffset) {
                anchorOffset[anchor] = toffset;
                anchorRank[anchor] = a + i;
            }
        }

    }

    /**
     * This routines sorts a[0] ... a[n-1] using the fact that in their common prefix, after offset characters, there is
     * a suffix whose rank is known. In this routine we call this suffix anchor (and we denote its position and rank
     * with anchor_pos and anchor_rank respectively) but it is not necessarily an anchor (=does not necessarily starts
     * at position multiple of anchorDist) since this function is called by pseudo_anchor_sort(). The routine works by
     * scanning the suffixes before and after the anchor in order to find (and mark) those which are suffixes of a[0]
     * ... a[n-1]. After that, the ordering of a[0] ... a[n-1] is derived with a sigle scan of the marked
     * suffixes.*******************************************************************
     */
    private void generalAnchorSort(int a, int n, int anchor_pos, int anchor_rank, int offset) {
        int sb, lo, hi;
        int curr_lo, curr_hi, to_be_found, i, j;
        int item;
        int ris;
        // void *ris;

        /* ---------- get bucket of anchor ---------- */
        sb = getSmallBucket(anchor_pos);
        lo = bucketFirst(sb);
        hi = bucketLast(sb);
        // ------ sort pointers a[0] ... a[n-1] as plain integers
        // qsort(a, n, sizeof(Int32), integer_cmp);
        Arrays.sort(suffixArray, a, a + n);

        // ------------------------------------------------------------------
        // now we scan the bucket containing the anchor in search of suffixes
        // corresponding to the ones we have to sort. When we find one of
        // such suffixes we mark it. We go on untill n sfx's have been marked
        // ------------------------------------------------------------------
        curr_hi = curr_lo = anchor_rank;

        mark(curr_lo);
        // scan suffixes preceeding and following the anchor
        for (to_be_found = n - 1; to_be_found > 0;) {
            // invariant: the next positions to check are curr_lo-1 and curr_hi+1
            assert (curr_lo > lo || curr_hi < hi);
            while (curr_lo > lo) {
                item = suffixArray[--curr_lo] - offset;
                ris = Arrays.binarySearch(suffixArray, a, a + n, item);
                // ris = bsearch(&item,a,n,sizeof(Int32), integer_cmp);
                if (ris != 0) {
                    mark(curr_lo);
                    to_be_found--;
                } else
                    break;
            }
            while (curr_hi < hi) {
                item = suffixArray[++curr_hi] - offset;
                ris = Arrays.binarySearch(suffixArray, a, a + n, item);
                if (ris != 0) {
                    mark(curr_hi);
                    to_be_found--;
                } else
                    break;
            }
        }
        // sort a[] using the marked suffixes
        for (j = 0, i = curr_lo; i <= curr_hi; i++)
            if (isMarked(i)) {
                unmark(i);
                suffixArray[a + j++] = suffixArray[i] - offset;
            }

    }

    /**
     *
     */
    private void unmark(int i) {
        suffixArray[i] &= ~MARKER;

    }

    /**
     *
     */
    private boolean isMarked(int i) {
        return (suffixArray[i] & MARKER) != 0;
    }

    /**
     *
     */
    private void mark(int i) {
        suffixArray[i] |= MARKER;

    }

    /**
     *
     */
    private int bucketLast(int sb) {
        return (ftab[sb + 1] & CLEARMASK) - 1;
    }

    /**
     *
     */
    private int bucketFirst(int sb) {
        return ftab[sb] & CLEARMASK;
    }

    /**
     *
     */
    private int bucketSize(int sb) {
        return (ftab[sb + 1] & CLEARMASK) - (ftab[sb] & CLEARMASK);
    }

    /**
     *
     */
    private int getSmallBucket(int pos) {
        return (text[this.start + pos] << 8) + text[this.start + pos + 1];
    }

    /**
     *
     */
    @SuppressWarnings("unused")
    private void pseudoOrDeepSort(int a, int n, int depth) {
        int offset, text_pos, sb, pseudo_anchor_pos, max_offset, size;

        // ------- search for a useful pseudo-anchor -------------
        if (MAX_PSEUDO_ANCHOR_OFFSET > 0) {
            max_offset = min(depth - 1, MAX_PSEUDO_ANCHOR_OFFSET);
            text_pos = suffixArray[a];
            for (offset = 1; offset < max_offset; offset++) {
                pseudo_anchor_pos = text_pos + offset;
                sb = getSmallBucket(pseudo_anchor_pos);
                // check if pseudo_anchor is in a sorted bucket
                if (isSortedBucket(sb)) {
                    size = bucketSize(sb); // size of group
                    if (size > B2G_RATIO * n)
                        continue; // discard large groups
                    // sort a[0] ... a[n-1] using pseudo_anchor
                    pseudoAnchorSort(a, n, pseudo_anchor_pos, offset);
                    return;
                }
            }
        }
        deepSort(a, n, depth);
    }

    /**
     *
     */
    private boolean isSortedBucket(int sb) {
        return (ftab[sb] & SETMASK) != 0;
    }

    /**
     * routine for deep-sorting the suffixes a[0] ... a[n-1] knowing that they have a common prefix of length "depth"
     */
    private void deepSort(int a, int n, int depth) {
        int blind_limit;

        blind_limit = textSize / BLIND_SORT_RATIO;
        if (n <= blind_limit)
            blindSsort(a, n, depth); // small_group
        else
            qsUnrolledLcp(a, n, depth, blind_limit);

    }

    /**
     * ternary quicksort (seward-like) with lcp information
     */
    private void qsUnrolledLcp(int a, int n, int depth, int blind_limit) {
        int text_depth, text_pos_pivot;// pointers
        int[] stack_lo = new int[STACK_SIZE];
        int[] stack_hi = new int[STACK_SIZE];
        int[] stack_d = new int[STACK_SIZE];
        int sp, r, r3, med;
        int i, j, lo, hi, ris, lcp_lo, lcp_hi;
        // ----- init quicksort --------------
        r = sp = 0;
        // Pushd(0,n-1,depth);
        stack_lo[sp] = 0;
        stack_hi[sp] = n - 1;
        stack_d[sp] = depth;
        sp++;
        // end Pushd

        // ----- repeat untill stack is empty ------
        while (sp > 0) {
            assert (sp < STACK_SIZE);
            // Popd(lo,hi,depth);
            sp--;
            lo = stack_lo[sp];
            hi = stack_hi[sp];
            depth = stack_d[sp];
            // end popd
            text_depth = depth;

            // --- use shellsort for small groups
            if (hi - lo < blind_limit) {
                blindSsort(a + lo, hi - lo + 1, depth);
                continue;
            }

            /*
             * Random partitioning. Guidance for the magic constants 7621 and 32768 is
             * taken from Sedgewick's algorithms book, chapter 35.
             */
            r = ((r * 7621) + 1) % 32768;
            r3 = r % 3;
            if (r3 == 0)
                med = lo;
            else if (r3 == 1)
                med = (lo + hi) >> 1;
            else
                med = hi;

            // --- partition ----
            swap(med, hi, a); // put the pivot at the right-end
            text_pos_pivot = text_depth + suffixArray[a + hi];
            i = lo - 1;
            j = hi;
            lcp_lo = lcp_hi = Integer.MAX_VALUE;
            while (true) {
                while (++i < hi) {
                    ris = cmpUnrolledLcp(text_depth + suffixArray[a + i], text_pos_pivot);
                    if (ris > 0) {
                        if (cmpDone < lcp_hi)
                            lcp_hi = cmpDone;
                        break;
                    } else if (cmpDone < lcp_lo)
                        lcp_lo = cmpDone;
                }
                while (--j > lo) {
                    ris = cmpUnrolledLcp(text_depth + suffixArray[a + j], text_pos_pivot);
                    if (ris < 0) {
                        if (cmpDone < lcp_lo)
                            lcp_lo = cmpDone;
                        break;
                    } else if (cmpDone < lcp_hi)
                        lcp_hi = cmpDone;
                }
                if (i >= j)
                    break;
                swap(i, j, a);
            }
            swap(i, hi, a); // put pivot at the middle

            // --------- insert subproblems in stack; smallest last
            if (i - lo < hi - i) {
                // Pushd(i + 1, hi, depth + lcp_hi);
                stack_lo[sp] = i + 1;
                stack_hi[sp] = hi;
                stack_d[sp] = depth + lcp_hi;
                sp++;
                // end pushd
                if (i - lo > 1) {
                    // Pushd(lo, i - 1, depth + lcp_lo);
                    stack_lo[sp] = lo;
                    stack_hi[sp] = i - 1;
                    stack_d[sp] = depth + lcp_lo;
                    sp++;
                    // end push
                }

            } else {
                // Pushd(lo, i - 1, depth + lcp_lo);
                stack_lo[sp] = lo;
                stack_hi[sp] = i - 1;
                stack_d[sp] = depth + lcp_lo;
                sp++;
                // end pushd
                if (hi - i > 1) {
                    // Pushd(i + 1, hi, depth + lcp_hi);
                    stack_lo[sp] = i + 1;
                    stack_hi[sp] = hi;
                    stack_d[sp] = depth + lcp_hi;
                    sp++;
                    // end pushd
                }
            }
        }

    }

    /**
     * Function to compare two strings originating from the *b1 and *b2 The size of the unrolled loop must be at most
     * equal to the costant CMP_OVERSHOOT defined in common.h the function return the result of the comparison (+ or -)
     * and writes in cmpDone the number of successfull comparisons done
     */
    private int cmpUnrolledLcp(int b1, int b2) {

        int c1, c2;
        cmpDone = 0;

        // execute blocks of 16 comparisons untill a difference
        // is found or we run out of the string
        do {
            // 1
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 2
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 1;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 3
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 2;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 4
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 3;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 5
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 4;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 6
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 5;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 7
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 6;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 8
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 7;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 9
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 8;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 10
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 9;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 11
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 10;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 12
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 11;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 13
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 12;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 14
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 13;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 15
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 14;
                return (c1 - c2);
            }
            b1++;
            b2++;
            // 16
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmpDone += 15;
                return (c1 - c2);
            }
            b1++;
            b2++;

            cmpDone += 16;

        } while (b1 < textSize && b2 < textSize);

        return b2 - b1;

    }

    /**
     *
     */
    private void swap(int i, int j, int a) {
        int tmp = suffixArray[a + i];
        suffixArray[a + i] = suffixArray[a + j];
        suffixArray[a + j] = tmp;
    }

    /**
     * routine for deep-sorting the suffixes a[0] ... a[n-1] knowing that they have a common prefix of length "depth"
     */
    private void blindSsort(int a, int n, int depth) {
        int i, j, aj, lcp;
        Node nh, root, h;

        // ---- sort suffixes in order of increasing length
        // qsort(a, n, sizeof(Int32), neg_integer_cmp);
        Arrays.sort(suffixArray, a, a + n);
        for (int left = 0, right = n - 1; left < right; left++, right--) {
            // exchange the first and last
            int temp = suffixArray[a + left];
            suffixArray[a + left] = suffixArray[a + right];
            suffixArray[a + right] = temp;
        }

        // --- skip suffixes which have already reached the end-of-text
        for (j = 0; j < n; j++)
            if (suffixArray[a + j] + depth < textSize)
                break;
        if (j >= n - 1)
            return; // everything is already sorted!

        // ------ init stack -------
        // stack = (node **) malloc(n*sizeof(node *));

        // ------- init root with the first unsorted suffix
        nh = new Node();
        nh.skip = -1;
        nh.right = null;
        // nh.down = (void *) a[j];
        nh.downInt = suffixArray[a + j];
        root = nh;

        // ------- insert suffixes a[j+1] ... a[n-1]
        for (i = j + 1; i < n; i++) {
            h = findCompanion(root, suffixArray[a + i]);
            aj = h.downInt;
            lcp = compareSuffixes(aj, suffixArray[a + i], depth);
            insertSuffix(root, suffixArray[a + i], lcp, text[this.start + aj + lcp]);
        }

        // ---- traverse the trie and get suffixes in lexicographic order
        aux = a;
        auxWritten = j;
        traverseTrie(root);

    }

    /**
     * this procedures traverse the trie in depth first order so that the suffixes (stored in the leaf) are recovered in
     * lexicographic order
     */
    private void traverseTrie(Node h) {
        Node p, nextp;

        if (h.skip < 0)
            suffixArray[aux + auxWritten++] = h.downInt;
        else {
            p = h.down;
            do {
                nextp = p.right;
                if (nextp != null) {
                    // if there are 2 nodes with equal keys
                    // they must be considered in inverted order
                    if (nextp.key == p.key) {
                        traverseTrie(nextp);
                        traverseTrie(p);
                        p = nextp.right;
                        continue;
                    }
                }
                traverseTrie(p);
                p = nextp;
            } while (p != null);
        }

    }

    /**
     * insert a suffix in the trie rooted at *p. we know that the trie already contains a string which share the first n
     * chars with suf
     */
    private void insertSuffix(Node h, int suf, int n, int mmchar) {
        int c, s;
        Node p, pp;

        s = suf;

        // --------- insert a new node before node *h if necessary
        if (h.skip != n) {
            p = new Node();
            p.key = mmchar;
            p.skip = h.skip; // p inherits skip and children of *h
            p.down = h.down;
            p.downInt = h.downInt;
            p.right = null;
            h.skip = n;
            h.down = p; // now *h has p as the only child
        }

        // -------- search the position of s[n] among *h offsprings
        c = text[this.start + s + n];
        pp = h.down;
        while (pp != null) {
            if (pp.key >= c)
                break;
            pp = pp.right;
        }
        // ------- insert new node containing suf
        p = new Node();
        p.skip = -1;
        p.key = c;
        p.right = pp;
        pp = p;
        p.downInt = suf;

    }

    /**
     * this function returns the lcp between suf1 and suf2 (that is returns n such that suf1[n]!=suf2[n] but
     * suf1[i]==suf2[i] for i=0..n-1 However, it is possible that suf1 is a prefix of suf2 (not vice-versa because of
     * the initial sorting of suffixes in order of descreasing length) in this case the function returns
     * n=length(suf1)-1. So in this case suf1[n]==suf2[n] (and suf1[n+1] does not exists).
     */
    private int compareSuffixes(int suf1, int suf2, int depth) {
        int limit;
        int s1, s2;

        s1 = depth + suf1;
        s2 = depth + suf2;
        limit = textSize - suf1 - depth;
        return depth + getLcpUnrolled(s1, s2, limit);
    }

    /**
     *
     */
    private int getLcpUnrolled(int b1, int b2, int cmp_limit) {
        int cmp2do;
        int c1, c2;

        // execute blocks of 16 comparisons untill a difference
        // is found or we reach cmp_limit comparisons
        cmp2do = cmp_limit;
        do {
            // 1
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                break;
            }
            b1++;
            b2++;
            // 2
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 1;
                break;
            }
            b1++;
            b2++;
            // 3
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 2;
                break;
            }
            b1++;
            b2++;
            // 4
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 3;
                break;
            }
            b1++;
            b2++;
            // 5
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 4;
                break;
            }
            b1++;
            b2++;
            // 6
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 5;
                break;
            }
            b1++;
            b2++;
            // 7
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 6;
                break;
            }
            b1++;
            b2++;
            // 8
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 7;
                break;
            }
            b1++;
            b2++;
            // 9
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 8;
                break;
            }
            b1++;
            b2++;
            // 10
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 9;
                break;
            }
            b1++;
            b2++;
            // 11
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 10;
                break;
            }
            b1++;
            b2++;
            // 12
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 11;
                break;
            }
            b1++;
            b2++;
            // 13
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 12;
                break;
            }
            b1++;
            b2++;
            // 14
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 13;
                break;
            }
            b1++;
            b2++;
            // 15
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 14;
                break;
            }
            b1++;
            b2++;
            // 16
            c1 = text[this.start + b1];
            c2 = text[this.start + b2];
            if (c1 != c2) {
                cmp2do -= 15;
                break;
            }
            b1++;
            b2++;

            cmp2do -= 16;
        } while (cmp2do > 0);

        if (cmp_limit - cmp2do < cmp_limit)
            return cmp_limit - cmp2do;

        return cmp_limit - 1;
    }

    /**
     * this function traverses the trie rooted at head following the string s. Returns the leaf "corresponding" to the
     * string s
     */
    private Node findCompanion(Node head, int s) {
        int c;
        Node p;
        int t;

        stackSize = 0; // init stack
        while (head.skip >= 0) {
            stack[stackSize++] = head;
            t = head.skip;
            if (s + t >= textSize) // s[t] does not exist: mismatch
                return getLeaf(head);
            c = text[this.start + s + t];
            p = head.down;
            boolean repeat = true;
            while (repeat) {
                if (c == p.key) { // found branch corresponding to c
                    head = p;
                    repeat = false;
                } else if (c < p.key) // no branch corresponding to c: mismatch
                {
                    return getLeaf(head);
                }
                if (repeat && (p = (p.right)) == null) // no other branches: mismatch
                {
                    return getLeaf(head);
                }
            }
        }
        stack[stackSize++] = head;
        return head;
    }

    /**
     * this function returns a leaf below "head". any leaf will do for the algorithm: we take the easiest to reach
     */
    private Node getLeaf(Node head) {
        Tools.assertAlways(head.skip >= 0, "");
        do {
            head = head.down;
        } while (head.skip >= 0);
        return head;
    }

    /**
     *
     */
    @SuppressWarnings("unused")
    private void pseudoAnchorSort(int a, int n, int pseudo_anchor_pos, int offset) {
        int pseudo_anchor_rank;

        // ---------- compute rank ------------
        if (UPDATE_ANCHOR_RANKS && anchorDist > 0)
            pseudo_anchor_rank = getRankUpdateAnchors(pseudo_anchor_pos);
        else
            pseudo_anchor_rank = getRank(pseudo_anchor_pos);
        // ---------- check rank --------------
        assert (suffixArray[pseudo_anchor_rank] == pseudo_anchor_pos);
        // ---------- do the sorting ----------
        generalAnchorSort(a, n, pseudo_anchor_pos, pseudo_anchor_rank, offset);

    }

    /**
     * compute the rank of the suffix starting at pos. It is required that the suffix is in an already sorted bucket
     */
    private int getRank(int pos) {
        int sb, lo, hi, j;

        sb = getSmallBucket(pos);
        if (!isSortedBucket(sb)) {
            throw new RuntimeException("Illegal call to get_rank! (get_rank1)");
        }
        lo = bucketFirst(sb);
        hi = bucketLast(sb);
        for (j = lo; j <= hi; j++)
            if (suffixArray[j] == pos)
                return j;
        throw new RuntimeException("Illegal call to get_rank! (get_rank2)");
    }

    /**
     * compute the rank of the suffix starting at pos. At the same time check if the rank of the suffixes in the bucket
     * containing pos can be used to update some entries in anchorOffset[] and anchorRank[] It is required that the
     * suffix is in an already sorted bucket
     */
    private int getRankUpdateAnchors(int pos) {
        int sb, lo, hi, j, toffset, aoffset, anchor, rank;

        // --- get bucket and verify it is a sorted one
        sb = getSmallBucket(pos);
        if (!(isSortedBucket(sb))) {
            throw new RuntimeException("Illegal call to get_rank! (get_rank_update_anchors)");
        }
        // --- if the bucket has been already ranked just compute rank;
        if (bucketRanked[sb] != 0)
            return getRank(pos);
        // --- rank all the bucket
        bucketRanked[sb] = 1;
        rank = -1;
        lo = bucketFirst(sb);
        hi = bucketLast(sb);
        for (j = lo; j <= hi; j++) {
            // see if we can update an anchor
            toffset = suffixArray[j] % anchorDist;
            anchor = suffixArray[j] / anchorDist;
            aoffset = anchorOffset[anchor]; // dist of sorted suf from anchor
            if (toffset < aoffset) {
                anchorOffset[anchor] = toffset;
                anchorRank[anchor] = j;
            }
            // see if we have found the rank of pos, if so store it in rank
            if (suffixArray[j] == pos) {
                assert (rank == -1);
                rank = j;
            }
        }
        assert (rank >= 0);
        return rank;
    }

    private void swap2(int a, int b) {
        int tmp = suffixArray[a];
        suffixArray[a] = suffixArray[b];
        suffixArray[b] = tmp;

    }

    /*
     * #define ptr2char32(i) (getword32(*(i) + text_depth))
     */
    private int ptr2char32(int a, int depth) {
        return getword32(suffixArray[a] + depth);
    }

    /*
     * #define getword32(s) ((unsigned)( (*(s) << 24) | ((*((s)+1)) << 16) \ | ((*((s)+2))
     * << 8) | (*((s)+3)) ))
     */
    private int getword32(int s) {
        return text[this.start + s] << 24 | text[this.start + s + 1] << 16 | text[this.start + s + 2] << 8 | text[this.start + s + 3];
    }

    private int ptr2char(int i, int text_depth) {
        return text[this.start + suffixArray[i] + text_depth];
    }

    private int med3(int a, int b, int c, int depth) {
        int va = ptr2char(a, depth);
        int vb = ptr2char(b, depth);
        if (va == vb) {
            return a;
        }
        int vc = ptr2char(c, depth);
        if (vc == va || vc == vb) {
            return c;
        }
        return va < vb ? (vb < vc ? b : (va < vc ? c : a)) : (vb > vc ? b : (va < vc ? a : c));
    }

    private void calculateRunningOrder() {
        int i, j;
        for (i = 0; i <= 256; i++)
            runningOrder[i] = i;
        {
            int vv;
            int h = 1;
            do
                h = 3 * h + 1;
            while (h <= 257);
            do {
                h = h / 3;
                for (i = h; i <= 256; i++) {
                    vv = runningOrder[i];
                    j = i;
                    while (bigFreq(runningOrder[j - h]) > bigFreq(vv)) {
                        runningOrder[j] = runningOrder[j - h];
                        j = j - h;
                        if (j <= (h - 1))
                            break;
                    }
                    runningOrder[j] = vv;
                }
            } while (h != 1);
        }
    }

    /**
     *
     */
    private int bigFreq(int b) {
        return ftab[((b) + 1) << 8] - ftab[(b) << 8];
    }

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            System.gc();
        }
        int size = 1000000;
        final Runtime rt = Runtime.getRuntime();
        long before, after;
        Node[] nodes = new Node[size];
        before = rt.totalMemory() - rt.freeMemory();
        for (int i = 0; i < size; i++) {
            nodes[i] = new Node();
        }
        after = rt.totalMemory() - rt.freeMemory();

        double a = 1.0 * (after - before) / size;

        System.out.println(before + " " + after + " " + size + " " + a);

    }
}
