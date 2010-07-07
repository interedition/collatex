/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.tokenization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sd_editions.collatex.iterator.ArrayIterator;

// NOTE: normalize == remove punctuation!
public class WitnessTokenizer {
  private final ArrayIterator iterator;
  private final boolean normalize;
  private final static Pattern SPLITTER = Pattern.compile("\\s+");
  private final static Pattern PUNCT = Pattern.compile("\\p{Punct}");

  // Note: alternative pattern!
  //original.toLowerCase().replaceAll("[`~'!@#$%^&*():;,\\.]", ""); 

  public WitnessTokenizer(String witness, boolean _normalize) {
    this.normalize = _normalize;
    String[] tokens = witness.isEmpty() ? new String[0] : SPLITTER.split(witness.trim());
    iterator = new ArrayIterator(tokens);
  }

  public boolean hasNextToken() {
    return iterator.hasNext();
  }

  public Token nextToken() {
    String token = (String) iterator.next();
    String original = token;
    String text = token;
    String punctuation = "";
    if (normalize) {
      Matcher matcher = PUNCT.matcher(token);
      boolean find = matcher.find();
      if (find) {
        punctuation = matcher.group();
        text = matcher.replaceAll("");
      }
      text = text.toLowerCase();
    }
    Token t = new Token(original, text, punctuation);
    return t;
  }
}
