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

package eu.interedition.collatex.simple;

import java.util.function.Function;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author Ronald Haentjens Dekker
 */
public class SimpleTokenNormalizers {

    public static final Function<String, String> LOWER_CASE = String::toLowerCase;

    public static final Function<String, String> TRIM_WS = String::trim;

    public static final Function<String, String> TRIM_WS_PUNCT = input -> {
        int start = 0;
        int end = input.length() - 1;
        while (start <= end && isWhitespaceOrPunctuation(input.charAt(start))) {
            start++;
        }
        while (end >= start && isWhitespaceOrPunctuation(input.charAt(end))) {
            end--;
        }
        return input.substring(start, end + 1);
    };

    public static boolean isWhitespaceOrPunctuation(char c) {
        if (Character.isWhitespace(c)) {
            return true;
        }
        switch (Character.getType(c)) {
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
                return true;
            default:
                return false;
        }
    }

    public static final Function<String, String> LC_TRIM_WS_PUNCT = LOWER_CASE.andThen(TRIM_WS_PUNCT);

    public static final Function<String, String> LC_TRIM_WS = LOWER_CASE.andThen(TRIM_WS);
}
