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

package com.sd_editions.collatex.permutations;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import eu.interedition.collatex.input.Word;

/* Possible MultiMatch */
public class PMMatch {
  final MultiMatch match;
  private boolean fixed;

  public PMMatch(MultiMatch _match) {
    match = _match;
    fixed = false;
  }

  public void fix() {
    this.fixed = true;
  }

  public boolean isFixed() {
    return fixed;
  }

  @Override
  public String toString() {
    Function<Word, Integer> extractPosition = new Function<Word, Integer>() {
      @SuppressWarnings("boxing")
      @Override
      public Integer apply(Word word) {
        return word.position;
      }
    };
    // words.collect{|w| w.position}
    return "[" + Joiner.on("->").join(Iterables.transform(getWords(), extractPosition)) + "]" + (this.isFixed() ? " (fixed)" : " (potential)");
  }

  public List<Word> getWords() {
    return match.getWords();
  }

  public PMMatch copy() {
    PMMatch copy = new PMMatch(this.match);
    if (this.isFixed()) copy.fix();
    return copy;
  }
}
