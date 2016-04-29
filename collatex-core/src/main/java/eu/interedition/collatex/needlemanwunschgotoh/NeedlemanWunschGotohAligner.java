/*
 * Copyright (c) 2016 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.needlemanwunschgotoh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import eu.interedition.collatex.matching.Pair;

/**
 * A generic Needleman-Wunsch-Gotoh sequence aligner.
 *
 * This aligner aligns two sequences of type A and type B respectively.  It is
 * totally agnostic of the type of objects it aligns.
 *
 * The aligner needs a {@code NeedlemanWunschScorer} that determines the score
 * of a match between an object of type A and an object of type B.
 *
 * This implementation uses Gotoh's improvements to get $\mathcal{O}(mn)$
 * running time and reduce memory requirements to essentially the backtracking
 * matrix only.  In Gotoh's technique the gap weight formula must be of the
 * special form $w_k = uk + v$. $k$ is the gap size, $v$ is the gap opening
 * score and $u$ the gap extension score.
 *
 * See: Gotoh, O. (1982).  An Improved Algorithm for Matching Biological
 * Sequences, J. Mol. Biol. 162, 705-708
 *
 * @param <A> Type of first sequence
 * @param <B> Type of second sequence
 *
 * @author Marcello Perathoner
 */
public class NeedlemanWunschGotohAligner<A, B> {
    /** A Scorer. */
    private final NeedlemanWunschScorer<A, B> scorer;
    /** A human-readable matrix as string. Written to only if set. */
    private StringBuilder debugMatrix = null;
    /** The gap opening score. */
    private final double openScore;
    /** The gap extension score. */
    private final double extendScore;

    public NeedlemanWunschGotohAligner(final NeedlemanWunschScorer<A, B> scorer) {
        this(scorer, -1.0, -0.5);
    }

    public NeedlemanWunschGotohAligner(final NeedlemanWunschScorer<A, B> scorer,
                                       final double gapOpenScore,
                                       final double gapExtendScore) {
        this.scorer = scorer;
        this.openScore = gapOpenScore;
        this.extendScore = gapExtendScore;
    }

    private class Data {
        /** The current score. */
        public double score;
        /** $P_{m,n}, Q_{m,n} in Gotoh. */
        public double p;
        public double q;
        /** The size of the gap. $k$ in Gotoh. */
        public int    pSize;
        public int    qSize;

        Data(final double score) {
            this.score = score;
            this.p     = 0.0;
            this.q     = 0.0;
            this.pSize = 0;
            this.qSize = 0;
        }
    }

    public List<Pair<A, B>> align(final Collection<A> inputA, final Collection<B> inputB) {
        int i, j;

        final int sizeA = inputA.size();
        final int sizeB = inputB.size();

        // 0 j . B
        // i
        // .
        // A

        /**
         * The backtracking matrix.  0 stands for a match.  Negative numbers
         * represent a DEL TOP operation.  The abs() of the number is the length
         * of the gap.  Positive numbers represent an INS LEFT operation.  The
         * value of the muber is the length of the gap.
         */
        ArrayList<ArrayList<Integer>>   lenMatrix  = new ArrayList<>(sizeA + 1);
        /**
         * The scoring matrix. We need only the last row of the scoring matrix
         * for our calculations, so we allocate the scoring matrix only when
         * debugging.
         */
        ArrayList<ArrayList<Data>>      matrix     = null;
        /** The current row of the backtracking matrix. */
        ArrayList<Integer>              thisLenRow = new ArrayList<>(sizeB + 1);
        /** The current row of the scoring matrix. */
        ArrayList<Data>                 thisRow    = new ArrayList<>(sizeB + 1);

        // Initialize lenMatrix and one row of the scoring matrix.

        lenMatrix.add(thisLenRow);

        thisRow.add(new Data(0.0));
        thisLenRow.add(0);
        for (j = 1; j <= sizeB; ++j) {
            Data d = new Data(openScore + (j - 1) * extendScore);
            d.p = d.score;
            // d.pSize = j;
            thisRow.add(d);
            thisLenRow.add(j);
        }

        if (debugMatrix != null) {
            matrix = new ArrayList<>(sizeA + 1);
            matrix.add((ArrayList<Data>) thisRow.clone());
        }

        // Score the matrix
        i = 0;
        for (A a : inputA) {
            i++;

            // add new lenRow to matrix
            thisLenRow = new ArrayList<>(sizeB + 1);
            lenMatrix.add(thisLenRow);
            thisLenRow.add(-i); // DEL TOP

            Data diag = thisRow.get(0);
            Data left = new Data(openScore + (i - 1) * extendScore);
            left.q = left.score;
            // left.qSize = i;
            j = 0;
            for (B b : inputB) {
                j++;
                Data top = thisRow.get(j);
                Data curr = new Data(0.0);

                curr.p = top.score + openScore;
                curr.pSize = 1;
                if (curr.p < top.p + extendScore) {
                    curr.p = top.p + extendScore;
                    curr.pSize = top.pSize + 1;
                }
                curr.q = left.score + openScore;
                curr.qSize = 1;
                if (curr.q < left.q + extendScore) {
                    curr.q = left.q + extendScore;
                    curr.qSize = left.qSize + 1;
                }
                final double d = diag.score + scorer.score(a, b);

                // Decide which operation is optimal and perform it
                if ((d > curr.p) && (d > curr.q)) {
                    curr.score = d;
                    thisLenRow.add(0);
                } else if (curr.q > curr.p) {
                    curr.score = curr.q;
                    thisLenRow.add(curr.qSize); // INS LEFT
                } else {
                    curr.score = curr.p;
                    thisLenRow.add(-curr.pSize); // DEL TOP
                }

                // Advance to next column
                thisRow.set(j - 1, left);
                thisRow.set(j, curr);
                diag = top;
                left = curr;
            }

            if (matrix != null) {
                matrix.add((ArrayList<Data>) thisRow.clone());
            }
        }

        // Walk back and output alignments.  We need random access, so copy the
        // input Collections to ArrayLists.
        final LinkedList<Pair<A, B>> alignments = new LinkedList<>();
        final ArrayList<A> arrayA = new ArrayList<>(inputA);
        final ArrayList<B> arrayB = new ArrayList<>(inputB);
        i = sizeA;
        j = sizeB;
        while ((i > 0) || (j > 0)) {
            int len = lenMatrix.get(i).get(j);
            if (len == 0) {
                alignments.addFirst(new Pair<A, B>(arrayA.get(i - 1), arrayB.get(j - 1)));
                --i;
                --j;
            } else {
                if (len < 0) {
                    for (int k = 0; k < -len; ++k) {
                        alignments.addFirst(new Pair<A, B>(arrayA.get(i - 1), null));
                        --i;
                    }
                } else {
                    for (int k = 0; k < len; ++k) {
                        alignments.addFirst(new Pair<A, B>(null, arrayB.get(j - 1)));
                        --j;
                    }
                }
            }
        }

        if (matrix != null) {
            buildDebugMatrix(matrix, lenMatrix, arrayA, arrayB);
        }

        return alignments;
    }

    /**
     * Set a debug matrix.  The aligner will fill the debug matrix with a
     * human-readable representation of the Needleman-Wunsch matrix if the debug
     * matrix is set.
     *
     * @param debugMatrix A StringBuilder or null.
     */
    public void setDebugMatrix(final StringBuilder debugMatrix) {
        this.debugMatrix = debugMatrix;
    }

    /**
     * Build the debug matrix string.  Builds a human-readable matrix in a
     * string.
     *
     * @param matrix
     * @param lenMatrix
     * @param inputA
     * @param inputB
     */
    private void buildDebugMatrix(final ArrayList<ArrayList<Data>> matrix,
                                  final ArrayList<ArrayList<Integer>> lenMatrix,
                                  final ArrayList<A> inputA,
                                  final ArrayList<B> inputB) {

        debugMatrix.setLength(0);

        debugMatrix.append(String.format("%29s | ", ""));
        debugMatrix.append(String.format("%29s | ", ""));
        for (B b : inputB) {
            debugMatrix.append(String.format("%-29s | ", b));
        }
        debugMatrix.append("\n");

        for (int i = 0; i < matrix.size(); ++i) {
            debugAdd(matrix.get(i), lenMatrix.get(i), i > 0 ? inputA.get(i - 1).toString() : "");
        }
        debugMatrix.append("\n");
    }

    /**
     * Helper function.
     *
     * @param dataRow
     * @param lenRow
     * @param a
     */
    private void debugAdd(final ArrayList<Data> dataRow,
                          final ArrayList<Integer> lenRow,
                          final String a) {

        debugMatrix.append(String.format("%29s | ", a));
        for (int i = 0; i < dataRow.size(); ++i) {
            Data data = dataRow.get(i);
            int len = lenRow.get(i);
            if (len == 0) {
                debugMatrix.append("↖ ");
            } else {
                if (len < 0) {
                    debugMatrix.append("↑ ");
                } else {
                    debugMatrix.append("← ");
                }
            }
            debugMatrix.append(String.format("% 2.6f ", data.score));
            debugMatrix.append(String.format("% 2.2f ", data.p));
            debugMatrix.append(String.format("% 2d ",   data.pSize));
            debugMatrix.append(String.format("% 2.2f ", data.q));
            debugMatrix.append(String.format("% 2d | ", data.qSize));
        }
        debugMatrix.append("\n");
    }
}
