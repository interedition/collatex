/*
 * Copyright (c) 2013 The Interedition Development Group.
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

import com.google.common.base.Function;
import com.google.common.base.Functions;

import javax.annotation.Nullable;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleTokenNormalizers {

  public static final Function<String, String> LOWER_CASE = new Function<String, String>() {
    @Override
    public String apply(@Nullable String input) {
      return input.toLowerCase();
    }
  };

  public static final Function<String, String> TRIM_WS = new Function<String, String>() {
    @Override
    public String apply(@Nullable String input) {
      return input.trim();
    }
  };

  public static final Function<String, String> TRIM_WS_PUNCT = new Function<String, String>() {

    @Override
    public String apply(@Nullable String input) {
      int start = 0;
      int end = input.length() - 1;
      while (start <= end && isWhitespaceOrPunctuation(input.charAt(start))) {
        start++;
      }
      while (end >= start && isWhitespaceOrPunctuation(input.charAt(end))) {
        end--;
      }
      return input.substring(start, end + 1);
    }

    boolean isWhitespaceOrPunctuation(char c) {
      if (Character.isWhitespace(c)) {
        return true;
      }
      final int type = Character.getType(c);
      return (Character.START_PUNCTUATION == type || Character.END_PUNCTUATION == type || Character.OTHER_PUNCTUATION == type);
    }
  };

  public static final Function<String, String> LC_TRIM_WS_PUNCT = Functions.compose(LOWER_CASE, TRIM_WS_PUNCT);
}
