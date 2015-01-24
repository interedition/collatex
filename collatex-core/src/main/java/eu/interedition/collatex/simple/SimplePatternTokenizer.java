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

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author Ronald Haentjens Dekker
 */
public class SimplePatternTokenizer implements Function<String, Iterable<String>> {

  private final Pattern pattern;

  public SimplePatternTokenizer(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Iterable<String> apply(@Nullable String input) {
    final Matcher matcher = pattern.matcher(input);
    final List<String> tokens = new LinkedList<>();
    while (matcher.find()) {
      tokens.add(input.substring(matcher.start(), matcher.end()));
    }
    return tokens;
  }

  public static final SimplePatternTokenizer BY_WHITESPACE = new SimplePatternTokenizer(
          Pattern.compile("\\s*?\\S+\\s*]")
  );

  static final String PUNCT = Pattern.quote(".?!,;:");

  public static final SimplePatternTokenizer BY_WS_AND_PUNCT = new SimplePatternTokenizer(
          Pattern.compile("[\\s" + PUNCT + "]*?[^\\s" + PUNCT + "]+[\\s" + PUNCT + "]*")
  );
  
  public static final SimplePatternTokenizer BY_WS_OR_PUNCT = new SimplePatternTokenizer(
          Pattern.compile("[" + PUNCT + "]+[\\s]*|[^" + PUNCT + "\\s]+[\\s]*")
  );
}