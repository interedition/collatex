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

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author Ronald Haentjens Dekker
 */
public class SimplePatternTokenizer {

    static final String PUNCT = Pattern.quote(".?!,;:");

    static Function<String, Stream<String>> tokenizer(Pattern pattern) {
        return input -> {
            final Matcher matcher = pattern.matcher(input);
            final List<String> tokens = new LinkedList<>();
            while (matcher.find()) {
                tokens.add(input.substring(matcher.start(), matcher.end()));
            }
            return tokens.stream();
        };
    }

    public static final Function<String, Stream<String>> BY_WHITESPACE = tokenizer(Pattern.compile("\\s*?\\S+\\s*]"));

    public static final Function<String, Stream<String>> BY_WS_AND_PUNCT = tokenizer(Pattern.compile("[\\s" + PUNCT + "]*?[^\\s" + PUNCT + "]+[\\s" + PUNCT + "]*"));

    public static final Function<String, Stream<String>> BY_WS_OR_PUNCT = tokenizer(Pattern.compile("[" + PUNCT + "]+[\\s]*|[^" + PUNCT + "\\s]+[\\s]*"));

}