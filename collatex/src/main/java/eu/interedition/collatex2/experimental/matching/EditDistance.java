/*
 * Copyright 2002-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.interedition.collatex2.experimental.matching;

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
