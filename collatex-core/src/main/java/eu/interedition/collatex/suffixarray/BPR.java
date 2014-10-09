package eu.interedition.collatex.suffixarray;

import java.util.Arrays;

/**
 * <p>
 * A straightforward reimplementation of the bucket pointer refinement algorithm given in:
 * <tt>
 * Klaus-Bernd Schürmann, Suffix Arrays in Theory and Practice, Faculty of Technology of 
 * Bielefeld University, Germany, 2007
 * </tt>
 * <p>
 * This implementation is basically a translation of the C version given by Klaus-Bernd
 * Schürmann: <tt>http://bibiserv.techfak.uni-bielefeld.de/download/tools/bpr.html</tt>
 * <p>
 * The implementation of this algorithm makes some assumptions about the input. See
 * {@link #buildSuffixArray(int[], int, int)} for details.
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
public class BPR implements ISuffixArrayBuilder
{
    private final static class Alphabet
    {
        int size;
        int [] charArray;
        int [] alphaMapping;
        int [] charFreq;

        Alphabet(int [] thisString, int stringLength)
        {
            int tmpChar;
            size = 0;
            alphaMapping = new int [KBS_MAX_ALPHABET_SIZE];
            charFreq = new int [KBS_MAX_ALPHABET_SIZE];
            for (int i = 0; i < stringLength; i++)
            {
                tmpChar = thisString[i];
                Tools.assertAlways(tmpChar >= 0, "Input must be positive");
                if (charFreq[tmpChar] == 0)
                {
                    size++;
                }
                charFreq[tmpChar]++;
            }

            charArray = new int [size + 1];
            charArray[size] = 0;
            int k = 0;
            for (int i = 0; i < KBS_MAX_ALPHABET_SIZE; i++)
            {
                alphaMapping[i] = -1;
                if (charFreq[i] > 0)
                {
                    charArray[k] = i;
                    alphaMapping[i] = k;
                    k++;
                }
            }
            Tools.assertAlways(k == size, "k != size");
        }
    }

    public final static int KBS_MAX_ALPHABET_SIZE = 256;
    public final static int KBS_INSSORT_THRES_LEN = 15;
    public final static int KBS_STRING_EXTENSION_SIZE = 32;
    public final static int INSSORT_LIMIT = 15;

    /**
     * If <code>true</code>, {@link #buildSuffixArray(int[], int, int)} uses a copy of the
     * input so it is left intact.
     */
    private final boolean preserveInput;

    private int [] seq;
    private int length;
    private Alphabet alphabet;
    private int [] suffixArray;
    private int [] sufPtrMap;

    private int start;

    public BPR()
    {
        this(true);
    }

    public BPR(boolean preserveInput)
    {
        this.preserveInput = preserveInput;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additional constraints enforced by BPR algorithm:
     * <ul>
     * <li>input array must contain at least {@link #KBS_STRING_EXTENSION_SIZE} extra
     * cells</li>
     * <li>non-negative (&ge;0) symbols in the input</li>
     * <li>symbols limited by {@link #KBS_MAX_ALPHABET_SIZE} (&lt;
     * <code>KBS_MAX_ALPHABET_SIZE</code>)</li>
     * <li>length >= 2</li>
     * </ul>
     * <p>
     */
    @Override
    public int [] buildSuffixArray(int [] input, int start, int length)
    {
        Tools.assertAlways(input != null, "input must not be null");
        Tools.assertAlways(input.length >= start + length + KBS_STRING_EXTENSION_SIZE,
            "input is too short");
        Tools.assertAlways(length >= 2, "input length must be >= 2");
        this.start = start;

        if (preserveInput)
        {

            seq = new int [length + KBS_STRING_EXTENSION_SIZE];
            this.start = 0;
            System.arraycopy(input, start, seq, 0, length);
        }
        else
        {
            seq = input;
        }

        this.alphabet = new Alphabet(seq, length);
        this.length = length;
        int alphaSize = alphabet.size;

        int q;
        if (alphaSize <= 9)
        {
            q = 7;
        }
        else if (9 < alphaSize && alphaSize <= 13)
        {
            q = 6;
        }
        else if (13 < alphaSize && alphaSize <= 21)
        {
            q = 5;
        }
        else if (21 < alphaSize && alphaSize <= 46)
        {
            q = 4;
        }
        else
        {
            q = 3;
        }

        kbs_buildDstepUsePrePlusCopyFreqOrder_SuffixArray(q);
        return suffixArray;
    }

    /**
     * 
     */
    private void kbs_buildDstepUsePrePlusCopyFreqOrder_SuffixArray(int q)
    {
        int [] buckets = determine_Buckets_Sarray_Sptrmap(q);

        /* Sorting of all buckets */
        int mappedCharPtr = 0;
        int alphabetSize = alphabet.size;
        int bucketsInLevel3Bucket = kbs_power_Ulong(alphabetSize, q - 3);
        int bucketsInLevel2Bucket = bucketsInLevel3Bucket * alphabetSize;
        int bucketsInLevel1Bucket = bucketsInLevel2Bucket * alphabetSize;
        int [] alphaOrder = getCharWeightedOrder_Alphabet(buckets, bucketsInLevel2Bucket);
        int [] isNotSortedLevel1Char = new int [alphabetSize];
        Arrays.fill(isNotSortedLevel1Char, 1);

        /* Sort all level-1 buckets */
        int [] leftPtrList = new int [alphabetSize];
        int [] rightPtrList = new int [alphabetSize];
        int [] leftPtrList2 = new int [alphabetSize * alphabetSize];
        int [] rightPtrList2 = new int [alphabetSize * alphabetSize];
        int i;
        int j;
        int c1 = 0;
        for (i = 0; i < alphabetSize; i++)
        {
            c1 = alphaOrder[i];
            /* sort buckets cd to cz */
            for (j = i + 1; j < alphabetSize; j++)
            {
                int c2 = alphaOrder[j];
                int l;
                for (l = i; l < alphabetSize; l++)
                {
                    int c3 = alphaOrder[l];
                    int tmpUlong = c1 * bucketsInLevel1Bucket + c2
                        * bucketsInLevel2Bucket + c3 * bucketsInLevel3Bucket;
                    int k;
                    for (k = tmpUlong; k < tmpUlong + bucketsInLevel3Bucket; k++)
                    {
                        int leftPtr = buckets[k];
                        int rightPtr = buckets[k + 1] - 1;
                        if (rightPtr - leftPtr > 0)
                        {
                            if (rightPtr - leftPtr < INSSORT_LIMIT) insSortUpdateRecurse_SaBucket(
                                leftPtr, rightPtr, q, q);
                            else partitionUpdateRecurse_SaBucket(leftPtr, rightPtr, q, q);
                        }
                    }
                }
            }

            /* copy left buckets of cx */
            for (j = i; j < alphabetSize; j++)
            {
                int cp1 = alphaOrder[j];
                leftPtrList[cp1] = buckets[cp1 * bucketsInLevel1Bucket + c1
                    * bucketsInLevel2Bucket];
                int k;
                for (k = i + 1; k < alphabetSize; k++)
                {
                    int cp2 = alphaOrder[k];
                    leftPtrList2[cp2 * alphabetSize + cp1] = buckets[cp2
                        * bucketsInLevel1Bucket + cp1 * bucketsInLevel2Bucket + c1
                        * bucketsInLevel3Bucket];
                }
            }
            if (c1 == 0)
            {
                int cp1 = seq[start + mappedCharPtr + length - 1];
                int cp2 = seq[start + mappedCharPtr + length - 2];
                if (isNotSortedLevel1Char[cp1] != 0)
                {
                    leftPtrList[cp1]++;
                    leftPtrList2[cp1 * alphabetSize]++;
                    if (isNotSortedLevel1Char[cp2] != 0 && cp2 != c1)
                    {
                        suffixArray[leftPtrList2[cp2 * alphabetSize + cp1]] = length - 2;
                        sufPtrMap[length - 2] = leftPtrList2[cp2 * alphabetSize + cp1];
                        leftPtrList2[cp2 * alphabetSize + cp1]++;
                    }
                }
            }

            int leftPtr = buckets[c1 * bucketsInLevel1Bucket];
            while (leftPtr < leftPtrList[c1])
            {
                int cp1;
                int tmpUlong = suffixArray[leftPtr];
                if (tmpUlong != 0
                    && isNotSortedLevel1Char[cp1 = seq[start + mappedCharPtr + tmpUlong
                        - 1]] != 0)
                {
                    if (isNotSortedLevel1Char[seq[start + mappedCharPtr + tmpUlong + 1]] != 0)
                    {
                        int tmpUlongPtr = leftPtrList[cp1];
                        sufPtrMap[tmpUlong - 1] = tmpUlongPtr;
                        suffixArray[tmpUlongPtr] = tmpUlong - 1;
                    }
                    leftPtrList[cp1]++;
                    int cp2;
                    if (tmpUlong > 1
                        && isNotSortedLevel1Char[cp2 = seq[start + mappedCharPtr
                            + tmpUlong - 2]] != 0 && cp2 != c1)
                    {
                        int tmpUlongPtr = leftPtrList2[cp2 * alphabetSize + cp1]++;
                        sufPtrMap[tmpUlong - 2] = tmpUlongPtr;
                        suffixArray[tmpUlongPtr] = tmpUlong - 2;
                    }
                }
                leftPtr++;
            }

            /* copy right buckets of cx */
            for (j = i; j < alphabetSize; j++)
            {
                int cp1 = alphaOrder[j];
                int k;
                rightPtrList[cp1] = buckets[cp1 * bucketsInLevel1Bucket + (c1 + 1)
                    * bucketsInLevel2Bucket];
                for (k = i + 1; k < alphabetSize; k++)
                {
                    int cp2 = alphaOrder[k];
                    rightPtrList2[cp2 * alphabetSize + cp1] = buckets[cp2
                        * bucketsInLevel1Bucket + cp1 * bucketsInLevel2Bucket + (c1 + 1)
                        * bucketsInLevel3Bucket];
                }
            }
            int rightPtr = buckets[(c1 + 1) * bucketsInLevel1Bucket];
            while (leftPtr < rightPtr)
            {
                int cp1;
                rightPtr--;
                int tmpUlong = suffixArray[rightPtr];
                if (tmpUlong != 0
                    && isNotSortedLevel1Char[cp1 = seq[start + mappedCharPtr + tmpUlong
                        - 1]] != 0)
                {
                    rightPtrList[cp1]--;
                    if (isNotSortedLevel1Char[seq[start + mappedCharPtr + tmpUlong + 1]] != 0)
                    {
                        int tmpUlongPtr = rightPtrList[cp1];
                        sufPtrMap[tmpUlong - 1] = tmpUlongPtr;
                        suffixArray[tmpUlongPtr] = tmpUlong - 1;
                    }
                    int cp2;
                    if (tmpUlong > 1
                        && isNotSortedLevel1Char[cp2 = seq[start + mappedCharPtr
                            + tmpUlong - 2]] != 0 && cp2 != c1)
                    {
                        int tmpUlongPtr = --rightPtrList2[cp2 * alphabetSize + cp1];
                        sufPtrMap[tmpUlong - 2] = tmpUlongPtr;
                        suffixArray[tmpUlongPtr] = tmpUlong - 2;
                    }
                }
            }
            isNotSortedLevel1Char[c1] = 0;
        }

    }

    /**
     * Stably sorts a bucket at a refinement level regarding sort keys that are bucket
     * pointers in sufPtrMap with offset.
     * 
     * @param leftPtr points to the leftmost suffix of the current bucket.
     * @param rightPtr points to the rightmost suffix of the current bucket.
     * @param offset is the length of the common prefix of the suffixes (a multiple of q).
     * @param q is the initial prefix length used for the bucket sort. It also determines
     *            the increase of offset.
     */
    private void insSortUpdateRecurse_SaBucket(int leftPtr, int rightPtr, int offset,
        int q)
    {
        int rightTmpPtr = leftPtr + 1;
        while (rightTmpPtr <= rightPtr)
        {
            int tempValue = suffixArray[rightTmpPtr];
            int tempHashValue = sufPtrMap[suffixArray[rightTmpPtr] + offset];
            int leftTmpPtr = rightTmpPtr;
            while (leftTmpPtr > leftPtr
                && sufPtrMap[suffixArray[leftTmpPtr - 1] + offset] > tempHashValue)
            {
                suffixArray[leftTmpPtr] = suffixArray[leftTmpPtr - 1];
                leftTmpPtr--;
            }
            suffixArray[leftTmpPtr] = tempValue;
            rightTmpPtr++;
        }
        updatePtrAndRefineBuckets_SaBucket(leftPtr, rightPtr, offset, q);
    }

    /**
     * The function determines the subbuckets after refining this bucket and recursively
     * calls the refinement function for the subbuckets.
     * 
     * @param leftPtr points to the leftmost suffix of the current bucket.
     * @param rightPtr points to the rightmost suffix of the current bucket.
     * @param offset is the length of the common prefix of the suffixes (a multiple of q).
     * @param q is the initial prefix length used for the bucket sort. It also determines
     *            the increase of offset.
     */
    private void updatePtrAndRefineBuckets_SaBucket(int leftPtr, int rightPtr,
        int offset, int q)
    {
        /*
         * for all buckets with resp. pointer > rightPtr determine buckets via setting
         * sufPtrMap
         */
        int leftIntervalPtr = rightPtr;
        int rightIntervalPtr = rightPtr;
        int tmpPtr;
        while (leftPtr <= leftIntervalPtr
            && rightPtr < (tmpPtr = sufPtrMap[suffixArray[leftIntervalPtr] + offset]))
        {
            do
            {
                sufPtrMap[suffixArray[leftIntervalPtr]] = rightIntervalPtr;
                leftIntervalPtr--;
            }
            while (leftPtr <= leftIntervalPtr
                && sufPtrMap[suffixArray[leftIntervalPtr] + offset] == tmpPtr);
            rightIntervalPtr = leftIntervalPtr;
        }

        /*
         * since the sufPtrMap for the suffixes between leftPtr and rightPtr might change
         * in previous 2 steps
         */

        /*
         * determine the bucket concerning suffixptr+offset between leftPtr and rightPtr
         * separately
         */
        rightIntervalPtr = leftIntervalPtr;
        while (leftPtr <= leftIntervalPtr
            && leftPtr <= sufPtrMap[suffixArray[leftIntervalPtr] + offset]
            && sufPtrMap[suffixArray[leftIntervalPtr] + offset] <= rightPtr)
        {
            sufPtrMap[suffixArray[leftIntervalPtr]] = rightIntervalPtr;
            leftIntervalPtr--;
        }

        /*
         * for all buckets with resp. pointer+offset < leftPtr determine buckets via
         * setting sufPtrMap
         */
        /*
         * start with rightIntervalPtr which indicates leftend-1 of bucket with resp.
         * pointer+offset between
         */
        /* leftPtr and rightPtr */
        int middleRightPtr = rightIntervalPtr;
        int middleLeftPtr = leftIntervalPtr;
        rightIntervalPtr = leftIntervalPtr;
        while (leftPtr <= leftIntervalPtr)
        {
            int tmpPtr2 = sufPtrMap[suffixArray[leftIntervalPtr] + offset];
            do
            {
                sufPtrMap[suffixArray[leftIntervalPtr]] = rightIntervalPtr;
                leftIntervalPtr--;
            }
            while (leftPtr <= leftIntervalPtr
                && sufPtrMap[suffixArray[leftIntervalPtr] + offset] == tmpPtr2);
            rightIntervalPtr = leftIntervalPtr;
        }

        int newOffset = offset + q;
        if (sufPtrMap[suffixArray[leftPtr]] == rightPtr)
        {
            newOffset = computeDiffDepthBucket_SaBucket(leftPtr, rightPtr, newOffset, q);
        }
        int leftTmpPtr = leftPtr;
        while (leftTmpPtr < middleLeftPtr)
        {
            int rightTmpPtr = sufPtrMap[suffixArray[leftTmpPtr]];
            int tmpLong = rightTmpPtr - leftTmpPtr;
            if (tmpLong > 0)
            {
                if (tmpLong == 1)
                {
                    computeBucketSize2_SaBucket(leftTmpPtr, rightTmpPtr, newOffset, q);
                    leftTmpPtr = rightTmpPtr + 1;
                    continue;
                }
                if (tmpLong == 2)
                {
                    computeBucketSize3_SaBucket(leftTmpPtr, rightTmpPtr, newOffset, q);
                    leftTmpPtr = rightTmpPtr + 1;
                    continue;
                }
                insSortUpdateRecurse_SaBucket(leftTmpPtr, rightTmpPtr, newOffset, q);
            }
            leftTmpPtr = rightTmpPtr + 1;
        }
        /* for buckets refering to this bucket, the offset can be doubled */
        if (middleRightPtr > middleLeftPtr + 1)
        {
            if (middleRightPtr - middleLeftPtr == 2)
            {
                computeBucketSize2_SaBucket(middleLeftPtr + 1, middleRightPtr, Math.max(
                    2 * offset, newOffset), q);
            }
            else
            {
                if (middleRightPtr - middleLeftPtr == 3)
                {
                    computeBucketSize3_SaBucket(middleLeftPtr + 1, middleRightPtr, Math
                        .max(2 * offset, newOffset), q);
                }
                else
                {
                    insSortUpdateRecurse_SaBucket(middleLeftPtr + 1, middleRightPtr, Math
                        .max(2 * offset, newOffset), q);
                }
            }
        }
        leftTmpPtr = middleRightPtr + 1;
        while (leftTmpPtr < rightPtr)
        {
            int rightTmpPtr = sufPtrMap[suffixArray[leftTmpPtr]];
            int tmpLong = rightTmpPtr - leftTmpPtr;
            if (tmpLong > 0)
            {
                if (tmpLong == 1)
                {
                    computeBucketSize2_SaBucket(leftTmpPtr, rightTmpPtr, newOffset, q);
                    leftTmpPtr = rightTmpPtr + 1;
                    continue;
                }
                if (tmpLong == 2)
                {
                    computeBucketSize3_SaBucket(leftTmpPtr, rightTmpPtr, newOffset, q);
                    leftTmpPtr = rightTmpPtr + 1;
                    continue;
                }
                insSortUpdateRecurse_SaBucket(leftTmpPtr, rightTmpPtr, newOffset, q);
            }
            leftTmpPtr = rightTmpPtr + 1;
        }

    }

    /**
     * Completely sorts buckets of size 3.
     * 
     * @param leftPtr points to the leftmost suffix of the current bucket.
     * @param rightPtr points to the rightmost suffix of the current bucket.
     * @param q is the initial prefix length used for the bucket sort. It also determines
     *            the increase of offset.
     * @param offset is the length of the common prefix of the suffixes rounded down to a
     *            multiple of q.
     */
    private void computeBucketSize3_SaBucket(int leftPtr, int rightPtr, int offset, int q)
    {
        int newOffset = offset;
        while (sufPtrMap[suffixArray[leftPtr] + newOffset] == sufPtrMap[suffixArray[leftPtr + 1]
            + newOffset]
            && sufPtrMap[suffixArray[leftPtr + 1] + newOffset] == sufPtrMap[suffixArray[rightPtr]
                + newOffset])
        {
            newOffset += q;
        }
        if (sufPtrMap[suffixArray[leftPtr] + newOffset] > sufPtrMap[suffixArray[rightPtr]
            + newOffset])
        {
            int swapTmp = suffixArray[leftPtr];
            suffixArray[leftPtr] = suffixArray[rightPtr];
            suffixArray[rightPtr] = swapTmp;
        }
        if (sufPtrMap[suffixArray[leftPtr] + newOffset] > sufPtrMap[suffixArray[leftPtr + 1]
            + newOffset])
        {
            int swapTmp = suffixArray[leftPtr];
            suffixArray[leftPtr] = suffixArray[leftPtr + 1];
            suffixArray[leftPtr + 1] = swapTmp;
        }
        if (sufPtrMap[suffixArray[leftPtr + 1] + newOffset] > sufPtrMap[suffixArray[rightPtr]
            + newOffset])
        {
            int swapTmp = suffixArray[rightPtr];
            suffixArray[rightPtr] = suffixArray[leftPtr + 1];
            suffixArray[leftPtr + 1] = swapTmp;
        }
        if (sufPtrMap[suffixArray[leftPtr] + newOffset] == sufPtrMap[suffixArray[leftPtr + 1]
            + newOffset])
        {
            int suffix1 = suffixArray[leftPtr] + newOffset + q;
            int suffix2 = suffixArray[leftPtr + 1] + newOffset + q;
            while (sufPtrMap[suffix1] == sufPtrMap[suffix2])
            {
                suffix1 += q;
                suffix2 += q;
            }
            if (sufPtrMap[suffix1] > sufPtrMap[suffix2])
            {
                int tmpSwap = suffixArray[leftPtr];
                suffixArray[leftPtr] = suffixArray[leftPtr + 1];
                suffixArray[leftPtr + 1] = tmpSwap;
            }
            sufPtrMap[suffixArray[leftPtr]] = leftPtr;
            sufPtrMap[suffixArray[leftPtr + 1]] = leftPtr + 1;
            sufPtrMap[suffixArray[rightPtr]] = rightPtr;
            return;
        }
        if (sufPtrMap[suffixArray[leftPtr + 1] + newOffset] == sufPtrMap[suffixArray[rightPtr]
            + newOffset])
        {
            sufPtrMap[suffixArray[leftPtr]] = leftPtr;
            int suffix1 = suffixArray[leftPtr + 1] + newOffset + q;
            int suffix2 = suffixArray[rightPtr] + newOffset + q;
            while (sufPtrMap[suffix1] == sufPtrMap[suffix2])
            {
                suffix1 += q;
                suffix2 += q;
            }
            if (sufPtrMap[suffix1] > sufPtrMap[suffix2])
            {
                int tmpSwap = suffixArray[rightPtr];
                suffixArray[rightPtr] = suffixArray[leftPtr + 1];
                suffixArray[leftPtr + 1] = tmpSwap;
            }
            sufPtrMap[suffixArray[leftPtr + 1]] = leftPtr + 1;
            sufPtrMap[suffixArray[rightPtr]] = rightPtr;
            return;
        }
        sufPtrMap[suffixArray[leftPtr]] = leftPtr;
        sufPtrMap[suffixArray[leftPtr + 1]] = (leftPtr + 1);
        sufPtrMap[suffixArray[rightPtr]] = rightPtr;
    }

    /**
     * Completely sorts buckets of size 2.
     * 
     * @param leftPtr points to the leftmost suffix of the current bucket.
     * @param rightPtr points to the rightmost suffix of the current bucket.
     * @param offset is the length of the common prefix of the suffixes rounded down to a
     *            multiple of q.
     * @param q is the initial prefix length used for the bucket sort. It also determines
     *            the increase of offset.
     */
    private void computeBucketSize2_SaBucket(int leftPtr, int rightPtr, int offset, int q)
    {
        int suffix1 = suffixArray[leftPtr] + offset;
        int suffix2 = suffixArray[rightPtr] + offset;
        while (sufPtrMap[suffix1] == sufPtrMap[suffix2])
        {
            suffix1 += q;
            suffix2 += q;
        }
        if (sufPtrMap[suffix1] > sufPtrMap[suffix2])
        {
            int tmpSwap = suffixArray[leftPtr];
            suffixArray[leftPtr] = suffixArray[rightPtr];
            suffixArray[rightPtr] = tmpSwap;
        }
        sufPtrMap[suffixArray[leftPtr]] = leftPtr;
        sufPtrMap[suffixArray[rightPtr]] = rightPtr;

    }

    /**
     * Computes about the LCP of all suffixes in this bucket. It will be the newoffset.
     * 
     * @param leftPtr points to the leftmost suffix of the current bucket.
     * @param rightPtr points to the rightmost suffix of the current bucket.
     * @param offset is the length of the common prefix of the suffixes rounded down to a
     *            multiple of q.
     * @param q is the initial prefix length used for the bucket sort. It also determines
     *            the increase of offset.
     * @return the LCP of suffixes in this bucket (newoffset).
     */
    private int computeDiffDepthBucket_SaBucket(int leftPtr, int rightPtr, int offset,
        int q)
    {
        int lcp = offset;
        while (true)
        {
            int runPtr = leftPtr;
            int a = suffixArray[rightPtr];
            int tmpPtr = sufPtrMap[a + lcp];
            while (runPtr < rightPtr)
            {
                if (sufPtrMap[suffixArray[runPtr] + lcp] != tmpPtr)
                {
                    return lcp;
                }
                runPtr++;
            }
            lcp += q;
        }
    }

    /**
     * Ternary partitioning of buckets with Lomuto's scheme. Subbuckets of size 2 and 3
     * are directly sorted and partitions smaller than a given threshold are sorted by
     * insertion sort.
     * 
     * @param leftPtr points to the leftmost position of the current bucket.
     * @param rightPtr points to the rightmost position of the current bucket.
     * @param offset is the length of the common prefix of the suffixes (a multiple of q).
     * @param q is the initial prefix length used for the bucket sort. It also determines
     *            the increase of offset.
     */
    private void partitionUpdateRecurse_SaBucket(int leftPtr, int rightPtr, int offset,
        int q)
    {
        int pivot;
        int tmpSize = rightPtr - leftPtr;
        if (tmpSize < 10000)
        {
            tmpSize = tmpSize / 4;
            pivot = sufPtrMap[suffixArray[leftPtr + tmpSize] + offset];
            int pivotb = sufPtrMap[suffixArray[leftPtr + 2 * tmpSize] + offset];
            int pivotc = sufPtrMap[suffixArray[rightPtr - tmpSize] + offset];
            int medNumber = medianOfThreeUlong(pivot, pivotb, pivotc);
            int pivotPtr = leftPtr + tmpSize;
            if (medNumber > 0)
            {
                pivotPtr = (medNumber == 1) ? (leftPtr + 2 * tmpSize)
                    : (rightPtr - tmpSize);
                pivot = (medNumber == 1) ? pivotb : pivotc;
            }
            int swapTmp = suffixArray[pivotPtr];
            suffixArray[pivotPtr] = suffixArray[leftPtr];
            suffixArray[leftPtr] = swapTmp;
        }
        else
        {
            int [] keyPtrList = new int [9];
            tmpSize = tmpSize / 10;
            int i;
            for (i = 0; i < 9; i++)
            {
                keyPtrList[i] = leftPtr + (i + 1) * tmpSize;
            }
            /* insertion sort */
            for (i = 1; i < 9; i++)
            {
                int tempValue = keyPtrList[i];
                int tempHashValue = sufPtrMap[suffixArray[tempValue] + offset];
                int j = i - 1;
                while (j >= 0
                    && sufPtrMap[suffixArray[keyPtrList[j]] + offset] > tempHashValue)
                {
                    keyPtrList[j + 1] = keyPtrList[j];
                    j--;
                }
                keyPtrList[j + 1] = tempValue;
            }
            int swapTmp = suffixArray[keyPtrList[4]];
            suffixArray[keyPtrList[4]] = suffixArray[leftPtr];
            suffixArray[leftPtr] = swapTmp;
            pivot = sufPtrMap[suffixArray[leftPtr] + offset];
        }

        int pivotRightPtr = leftPtr + 1;
        while (pivotRightPtr <= rightPtr
            && sufPtrMap[suffixArray[pivotRightPtr] + offset] == pivot)
        {
            ++pivotRightPtr;
        }
        int smallerPivotPtr = pivotRightPtr;
        while (smallerPivotPtr <= rightPtr
            && sufPtrMap[suffixArray[smallerPivotPtr] + offset] < pivot)
        {
            smallerPivotPtr++;
        }

        int frontPtr = smallerPivotPtr - 1;
        while (frontPtr++ < rightPtr)
        {
            int sortkey = sufPtrMap[suffixArray[frontPtr] + offset];
            if (sortkey <= pivot)
            {
                int swapTmp = suffixArray[frontPtr];
                suffixArray[frontPtr] = suffixArray[smallerPivotPtr];
                suffixArray[smallerPivotPtr] = swapTmp;
                if (sortkey == pivot)
                {
                    suffixArray[smallerPivotPtr] = suffixArray[pivotRightPtr];
                    suffixArray[pivotRightPtr++] = swapTmp;
                }
                smallerPivotPtr++;
            }
        }
        /* vector swap the pivot elements */
        int numberSmaller = smallerPivotPtr - pivotRightPtr;
        if (numberSmaller > 0)
        {
            int swapsize = Math.min((pivotRightPtr - leftPtr), numberSmaller);
            int pivotRightTmpPtr = leftPtr + swapsize - 1;
            vectorSwap(leftPtr, pivotRightTmpPtr, smallerPivotPtr - 1);

            /* recursively sort < partition */
            if (numberSmaller == 1)
            {
                sufPtrMap[suffixArray[leftPtr]] = leftPtr;
            }
            else
            {
                if (numberSmaller == 2)
                {
                    computeBucketSize2_SaBucket(leftPtr, leftPtr + 1, offset, q);
                }
                else
                {
                    if (numberSmaller == 3) computeBucketSize3_SaBucket(leftPtr,
                        leftPtr + 2, offset, q);
                    else partitionUpdateRecurse_SaBucket(leftPtr, leftPtr + numberSmaller
                        - 1, offset, q);
                }
            }
        }

        /* update pivots and recursively sort = partition */
        int leftTmpPtr = leftPtr + numberSmaller;
        smallerPivotPtr--;
        if (leftTmpPtr == smallerPivotPtr)
        {
            sufPtrMap[suffixArray[leftTmpPtr]] = leftTmpPtr;
            if (leftTmpPtr == rightPtr) return;
        }
        else
        {
            int newOffset = (pivot == rightPtr) ? (2 * offset) : offset + q;
            if (leftTmpPtr + 1 == smallerPivotPtr)
            {
                computeBucketSize2_SaBucket(leftTmpPtr, smallerPivotPtr, newOffset, q);
                if (rightPtr == smallerPivotPtr) return;
            }
            else
            {
                if (leftTmpPtr + 2 == smallerPivotPtr)
                {
                    computeBucketSize3_SaBucket(leftTmpPtr, smallerPivotPtr, newOffset, q);
                    if (rightPtr == smallerPivotPtr) return;
                }
                else
                {
                    if (rightPtr == smallerPivotPtr)
                    {
                        newOffset = computeDiffDepthBucket_SaBucket(leftPtr
                            + numberSmaller, rightPtr, newOffset, q);
                        partitionUpdateRecurse_SaBucket(leftTmpPtr, rightPtr, newOffset,
                            q);
                        return;
                    }
                    while (leftTmpPtr <= smallerPivotPtr)
                    {
                        sufPtrMap[suffixArray[leftTmpPtr]] = smallerPivotPtr;
                        leftTmpPtr++;
                    }
                    if (smallerPivotPtr < leftPtr + numberSmaller + INSSORT_LIMIT)
                    {
                        insSortUpdateRecurse_SaBucket(leftPtr + numberSmaller,
                            smallerPivotPtr, newOffset, q);
                    }
                    else partitionUpdateRecurse_SaBucket(leftPtr + numberSmaller,
                        smallerPivotPtr, newOffset, q);
                }
            }
        }

        /* recursively sort > partition */
        ++smallerPivotPtr;
        if (smallerPivotPtr == rightPtr)
        {
            sufPtrMap[suffixArray[rightPtr]] = rightPtr;
            return;
        }
        if (smallerPivotPtr + 1 == rightPtr)
        {
            computeBucketSize2_SaBucket(smallerPivotPtr, rightPtr, offset, q);
            return;
        }
        if (smallerPivotPtr + 2 == rightPtr)
        {
            computeBucketSize3_SaBucket(smallerPivotPtr, rightPtr, offset, q);
            return;
        }
        partitionUpdateRecurse_SaBucket(smallerPivotPtr, rightPtr, offset, q);

    }

    /**
     * @param leftPtr points to the leftmost suffix of the first swap space.
     * @param rightPtr points to the rightmost suffix of the first swap space.
     * @param swapEndPtr points to the leftmost suffix of the second swap space.
     */
    private void vectorSwap(int leftPtr, int rightPtr, int swapEndPtr)
    {
        int swapTmp = suffixArray[swapEndPtr];
        while (leftPtr < rightPtr)
        {
            suffixArray[swapEndPtr] = suffixArray[rightPtr];
            swapEndPtr--;
            suffixArray[rightPtr] = suffixArray[swapEndPtr];
            rightPtr--;
        }
        suffixArray[swapEndPtr] = suffixArray[leftPtr];
        suffixArray[leftPtr] = swapTmp;

    }

    /**
     * Sorts the alphabet concerning some weight concerning cc bucket size and alphabet
     * frequency Only works for mapped string with alphabet [0,alphaSize]
     * 
     * @param buckets - the bucket table
     * @param bucketsInLevel2Bucket - number of subbuckets of level-2 buckets
     * @return the order of the alphabet according to the weight on buckets with same
     *         first and second character
     */
    private int [] getCharWeightedOrder_Alphabet(int [] buckets, int bucketsInLevel2Bucket)
    {
        int alphabetSize = alphabet.size;
        int [] charWeight = new int [alphabetSize];
        int tmpBucketFactor = bucketsInLevel2Bucket * (alphabetSize + 1);
        int i;
        for (i = 0; i < alphabetSize; i++)
        {
            charWeight[i] = alphabet.charFreq[i];
            charWeight[i] -= buckets[i * tmpBucketFactor + bucketsInLevel2Bucket]
                - buckets[i * tmpBucketFactor];
        }

        int [] targetCharArray = new int [alphabetSize + 1];
        for (i = 0; i < alphabetSize; i++)
        {
            targetCharArray[i] = i;
        }
        for (i = 1; i < alphabet.size; i++)
        {
            int tmpWeight = charWeight[i];
            int j = i;
            while (j > 0 && tmpWeight < charWeight[targetCharArray[j - 1]])
            {
                targetCharArray[j] = targetCharArray[j - 1];
                j--;
            }
            targetCharArray[j] = i;
        }
        return targetCharArray;
    }

    /**
     * Constructs all buckets w.r.t. q-gram size q, the up to prefix q sorted suffix
     * array, and the bucket-pointer table.
     * 
     * @param q size of q-gram.
     * @return Buckets containing pointers into the suffix array.
     */
    private int [] determine_Buckets_Sarray_Sptrmap(int q)
    {

        if (kbs_getExp_Ulong(2, alphabet.size) >= 0)
        {
            return determinePower2Alpha_Buckets_Sarray_Sptrmap(q);
        }
        else
        {
            return determineAll_Buckets_Sarray_Sptrmap(q);
        }
    }

    /**
     * Constructs all buckets w.r.t. q-gram size q, the up to prefix q sorted suffix
     * array, and the bucket-pointer table.
     * 
     * @param q size of q-gram.
     * @return Buckets containing pointers into the suffix array.
     * @see #determine_Buckets_Sarray_Sptrmap
     */
    private int [] determineAll_Buckets_Sarray_Sptrmap(int q)
    {
        int [] buckets = determineAll_Buckets_Sarray(q);
        int strLen = length;
        sufPtrMap = new int [strLen + 2 * q + 1];

        /* computation of first hashvalue */
        int alphabetSize = alphabet.size;
        int mappedUcharArray = 0;
        int tempPower = 1;
        int hashCode = 0;
        int i;
        for (i = q - 1; i >= 0; i--)
        {
            hashCode += seq[start + mappedUcharArray + i] * tempPower;
            tempPower *= alphabetSize;
        }
        int tempModulo = kbs_power_Ulong(alphabetSize, q - 1);
        mappedUcharArray += q;
        int j;
        for (j = 0; j < strLen - 1; j++)
        {
            sufPtrMap[j] = (buckets[hashCode + 1]) - 1;
            hashCode -= (seq[start + mappedUcharArray - q]) * tempModulo;
            hashCode *= alphabetSize;
            hashCode += seq[start + mappedUcharArray];
            mappedUcharArray++;
        }
        sufPtrMap[j] = buckets[hashCode];
        /* set the values in sufPtrMap[strLen..strLen+2*d] to [-1, -2, ..., -2*d] */
        int beginPtr = -1;
        for (j = strLen; j <= strLen + 2 * q; j++)
        {
            sufPtrMap[j] = beginPtr--;
        }
        return buckets;
    }

    /**
     * Constructs all buckets w.r.t. q-gram size and the up to prefix q sorted suffix
     * array. Call determine_Buckets_Sarray(const Kbs_Ustring *const ustring, register
     * const Kbs_Ulong q, Kbs_Ulong **suffixArrayPtr) instead
     * 
     * @param q size of q-gram.
     * @return Buckets containing pointers into the suffix array.
     * @see #determine_Buckets_Sarray_Sptrmap(int)
     */
    private int [] determineAll_Buckets_Sarray(int q)
    {

        int strLen = length;
        int alphabetSize = alphabet.size;
        int numberBuckets = kbs_power_Ulong(alphabetSize, q);
        int [] buckets = new int [numberBuckets + 1];
        for (int i = 0; i < q; i++)
        {
            seq[start + length + i] = alphabet.charArray[0];
        }
        for (int i = 0; i < KBS_STRING_EXTENSION_SIZE - q; i++)
        {
            seq[start + length + i + q] = 0;
        }
        /* computation of first hashvalue */
        int [] alphaMap = alphabet.alphaMapping;
        int mappedUcharArray = 0;
        int hashCode = 0;
        int tempPower = 1;
        int i;
        for (i = q - 1; i >= 0; i--)
        {
            hashCode += (seq[start + mappedUcharArray + i] = alphaMap[seq[start
                + mappedUcharArray + i]])
                * tempPower;
            tempPower *= alphabetSize;
        }
        int firstHashCode = hashCode;
        /* computation of the size of buckets */
        int tempModulo = kbs_power_Ulong(alphabetSize, q - 1);
        mappedUcharArray += q;
        buckets[hashCode]++;
        int j;
        for (j = 1; j < strLen; j++)
        {
            hashCode -= (seq[start + mappedUcharArray - q]) * tempModulo;
            hashCode *= alphabetSize;
            hashCode += seq[start + mappedUcharArray] = alphaMap[seq[start
                + mappedUcharArray]];
            mappedUcharArray++;
            buckets[hashCode]++;
        }
        /* update the alphabet for mapped string */
        for (j = 0; j < alphabetSize; j++)
        {
            alphabet.charFreq[j] = alphabet.charFreq[alphabet.charArray[j]];
            alphabet.charArray[j] = j;
            alphaMap[j] = j;
        }
        for (; j < KBS_MAX_ALPHABET_SIZE; j++)
        {
            alphaMap[j] = -1;
        }

        this.suffixArray = new int [strLen + 1];
        /* computation of the bucket pointers, pointers into the suffix array */
        for (j = 1; j <= numberBuckets; j++)
        {
            buckets[j] = buckets[j - 1] + buckets[j];
        }

        /* computation of the suffix array (buckets that are copied later are left out) */
        int [] charRank = getCharWeightedRank_Alphabet(buckets, q);

        mappedUcharArray = q;
        hashCode = firstHashCode;
        for (j = 0; j < strLen - 1; j++)
        {
            int c1;
            buckets[hashCode]--;
            if ((c1 = charRank[seq[start + mappedUcharArray - q]]) < charRank[seq[start
                + mappedUcharArray + 1 - q]]
                && c1 <= charRank[seq[start + mappedUcharArray + 2 - q]]) suffixArray[buckets[hashCode]] = j;
            hashCode -= (seq[start + mappedUcharArray - q]) * tempModulo;
            hashCode *= alphabetSize;
            hashCode += seq[start + mappedUcharArray];
            mappedUcharArray++;
        }

        buckets[hashCode]--;
        suffixArray[buckets[hashCode]] = strLen - 1;

        buckets[numberBuckets] = strLen;
        return buckets;
    }

    /**
     * Constructs all buckets w.r.t. q-gram size q, the up to prefix length q sorted
     * suffix array, and the bucket-pointer table.
     * 
     * @param q size of q-gram.
     * @return Buckets containing pointers into the suffix array.
     * @see #determine_Buckets_Sarray_Sptrmap
     */
    private int [] determinePower2Alpha_Buckets_Sarray_Sptrmap(int q)
    {
        int strLen = length;
        int exp2 = kbs_getExp_Ulong(2, alphabet.size);
        if (exp2 < 0)
        {
            throw new RuntimeException("value out of bounds");
        }
        int [] buckets = determinePower2Alpha_Buckets_Sarray(q);
        this.sufPtrMap = new int [strLen + 2 * q + 1];
        int mappedUcharArray = 0;
        int hashCode = 0;
        int j;
        for (j = 0; j < q; j++)
        {
            hashCode = hashCode << exp2;
            hashCode += seq[start + mappedUcharArray + j];
        }
        int tempModulo = 0;
        tempModulo = ~tempModulo;
        tempModulo = tempModulo << (exp2 * (q - 1));
        tempModulo = ~tempModulo;
        mappedUcharArray += q;
        for (j = 0; j < strLen - 1; j++)
        {
            sufPtrMap[j] = (buckets[hashCode + 1]) - 1;
            hashCode = hashCode & tempModulo;
            hashCode = hashCode << exp2;
            hashCode = hashCode | seq[start + mappedUcharArray];
            mappedUcharArray++;
        }
        sufPtrMap[j] = buckets[hashCode];
        int beginPtr = -1;
        for (j = strLen; j <= strLen + 2 * q; j++)
        {
            sufPtrMap[j] = beginPtr--;
        }

        return buckets;
    }

    private int kbs_power_Ulong(int base, int exp)
    {
        int p;
        if (exp == 0)
        {
            return 1;
        }
        if (exp == 1)
        {
            return base;
        }
        if (base == 4)
        {
            if (exp > 15)
            {
                throw new RuntimeException();
            }
            return 4 << (2 * (exp - 1));
        }
        p = 1;
        for (; exp > 0; --exp)
        {
            p = p * base;
        }
        return p;
    }

    /**
     * Constructs all buckets w.r.t. q-gram size q and the up to prefix q sorted suffix
     * array. Precondition: ustring->alphabet->alphaSize = 2^x for some x; otherwise, call
     * determine_Buckets_Sarray.
     * 
     * @param q size of q-gram.
     * @return Buckets containing pointers into the suffix array.
     * @see #determine_Buckets_Sarray_Sptrmap(int)
     */
    private int [] determinePower2Alpha_Buckets_Sarray(int q)
    {
        int exp2 = kbs_getExp_Ulong(2, alphabet.size);
        int strLen = length;
        int mappedUcharArray = 0;
        for (int i = 0; i < q; i++)
        {
            seq[start + length + i] = alphabet.charArray[0];
        }
        for (int i = length + q; i < length + KBS_STRING_EXTENSION_SIZE - q; i++)
        {
            seq[start + i] = 0;
        }
        int numberBuckets = kbs_power_Ulong(alphabet.size, q);
        int [] buckets = new int [numberBuckets + 1];
        int hashCode = 0;
        for (int j = 0; j < q; j++)
        {
            hashCode = hashCode << exp2;
            hashCode += (seq[start + mappedUcharArray + j] = alphabet.alphaMapping[seq[start
                + mappedUcharArray + j]]);
        }
        int firstHashCode = hashCode;

        int tempModulo = 0;
        tempModulo = ~tempModulo;
        tempModulo = tempModulo << (exp2 * (q - 1));
        tempModulo = ~tempModulo;
        mappedUcharArray += q;
        buckets[hashCode]++;

        for (int j = 1; j < strLen; j++)
        {
            hashCode = hashCode & tempModulo;
            hashCode = hashCode << exp2;
            hashCode = hashCode
                | (seq[start + mappedUcharArray] = alphabet.alphaMapping[seq[start
                    + mappedUcharArray]]);
            mappedUcharArray++;
            buckets[hashCode]++;
        }

        /* update the alphabet for mapped string */
        int j;
        for (j = 0; j < alphabet.size; j++)
        {
            alphabet.charFreq[j] = alphabet.charFreq[alphabet.charArray[j]];
            alphabet.charArray[j] = j;
            alphabet.alphaMapping[j] = j;
        }
        for (; j < KBS_MAX_ALPHABET_SIZE; j++)
        {
            alphabet.alphaMapping[j] = -1;
        }

        this.suffixArray = new int [strLen + 1];

        /* computation of the bucket pointers, pointers into the suffix array */
        for (j = 1; j <= numberBuckets; j++)
        {
            buckets[j] = buckets[j - 1] + buckets[j];
        }

        /* computation of the suffix array */
        int [] charRank = getCharWeightedRank_Alphabet(buckets, q);
        mappedUcharArray = q;
        hashCode = firstHashCode;
        for (j = 0; j < strLen - 1; j++)
        {
            int c1;
            buckets[hashCode]--;
            if ((c1 = charRank[seq[start + mappedUcharArray - q]]) < charRank[seq[start
                + mappedUcharArray + 1 - q]]
                && (c1 <= charRank[seq[start + mappedUcharArray + 2 - q]])) suffixArray[buckets[hashCode]] = j;
            hashCode = hashCode & tempModulo;
            hashCode = hashCode << exp2;
            hashCode = hashCode | (seq[start + mappedUcharArray]);

            mappedUcharArray++;
        }
        buckets[hashCode]--;
        suffixArray[buckets[hashCode]] = strLen - 1;

        buckets[numberBuckets] = strLen;
        return buckets;
    }

    /**
     * Sorts the alphabet regarding some weight according to cc bucket size and alphabet
     * frequency Only works for mapped string with alphabet [0,alphaSize]
     * 
     * @param buckets - the bucket table
     * @param q - the initial q-gram size
     * @return the rank of each character
     */
    private int [] getCharWeightedRank_Alphabet(int [] buckets, int q)
    {

        int alphabetSize = alphabet.size;
        int [] charWeight = new int [alphabetSize];
        int bucketsInLevel2Bucket = kbs_power_Ulong(alphabetSize, q - 2);
        int tmpBucketFactor = bucketsInLevel2Bucket * (alphabetSize + 1);
        int i;
        charWeight[0] = alphabet.charFreq[0];
        charWeight[0] -= buckets[bucketsInLevel2Bucket - 1];
        for (i = 1; i < alphabetSize - 1; i++)
        {
            charWeight[i] = alphabet.charFreq[i];
            charWeight[i] -= buckets[i * tmpBucketFactor + bucketsInLevel2Bucket - 1]
                - buckets[i * tmpBucketFactor - 1];
        }
        charWeight[alphabetSize - 1] = alphabet.charFreq[i];
        charWeight[alphabetSize - 1] -= buckets[(alphabetSize - 1) * tmpBucketFactor
            + bucketsInLevel2Bucket - 1]
            - buckets[(alphabetSize - 1) * tmpBucketFactor - 1];

        int [] targetCharArray = new int [alphabetSize];
        for (i = 0; i < alphabetSize; i++)
        {
            targetCharArray[i] = i;
        }
        /* insertion sort by charWeight */
        for (i = 1; i < alphabet.size; i++)
        {
            int tmpWeight = charWeight[i];
            int j = i;
            while (j > 0 && tmpWeight < charWeight[targetCharArray[j - 1]])
            {
                targetCharArray[j] = targetCharArray[j - 1];
                j--;
            }
            targetCharArray[j] = i;
        }
        int [] charRank = new int [alphabetSize + 1];
        for (i = 0; i < alphabetSize; i++)
        {
            charRank[targetCharArray[i]] = i;
        }
        return charRank;
    }

    /**
     * 
     */
    private int kbs_getExp_Ulong(int base, int value)
    {
        int exp = 0;
        int tmpValue = 1;
        while (tmpValue < value)
        {
            tmpValue *= base;
            exp++;
        }
        if (tmpValue == value)
        {
            return exp;
        }
        else
        {
            return -1;
        }

    }

    /**
     * @param a first key
     * @param b second key
     * @param c third key
     * @return 0 if a is the median, 1 if b is the median, 2 if c is the median.
     */
    private int medianOfThreeUlong(int a, int b, int c)
    {
        if (a == b || a == c)
        {
            return 0;
        }
        if (b == c)
        {
            return 2;
        }
        return a < b ? (b < c ? 1 : (a < c ? 2 : 0)) : (b > c ? 1 : (a < c ? 0 : 2));
    }
}
