/*
 * Copyright (c) 2015 The Interedition Development Group.
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

package eu.interedition.collatex.matching;

public final class EditDistance {
    private static final int MAX_DISTANCE_COMPARISON = 2500;

    public static int compute(String str1, String str2) {
        if ((str1.length() * str2.length() > MAX_DISTANCE_COMPARISON)) {
            return MAX_DISTANCE_COMPARISON;
        }

        final char[] str1Chars = str1.toCharArray();
        final int str1Length = str1Chars.length;
        final char[] str2Chars = str2.toCharArray();
        final int str2Length = str2Chars.length;

        if (str1Length == 0) {
            return str2Length;
        }
        if (str2Length == 0) {
            return str1Length;
        }

        int[][][] cache = new int[30][][];
        int matrix[][];
        if (str2Length >= cache.length) {
            matrix = form(str1Length, str2Length);
        } else if (cache[str2Length] != null) {
            matrix = cache[str2Length];
        } else {
            matrix = cache[str2Length] = form(str1Length, str2Length);
        }

        for (int i = 1; i <= str1Length; i++) {
            final char str1Char = str1Chars[i - 1];
            for (int j = 1; j <= str2Length; j++) {
                final char str2Char = str2Chars[j - 1];
                final int cost = (str1Char == str2Char ? 0 : 1);
                matrix[i][j] = min3(matrix[i - 1][j] + 1, matrix[i][j - 1] + 1, matrix[i - 1][j - 1] + cost);
            }
        }

        return matrix[str1Length][str2Length];
    }

    private static int[][] form(int n, int m) {
        int[][] d = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            d[i][0] = i;

        }
        for (int j = 0; j <= m; j++) {
            d[0][j] = j;
        }
        return d;
    }

    private static int min3(int a, int b, int c) {
        int mi = a;
        if (b < mi) {
            mi = b;
        }
        if (c < mi) {
            mi = c;
        }
        return mi;
    }

}
