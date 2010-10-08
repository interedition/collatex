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

package com.sd_editions.collatex.Web;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Word;

public class Alignment {
  private final List<Word> words;
  public final int color;

  public Alignment(int _color) {
    this.words = Lists.newArrayList();
    this.color = _color;
  }

  public void add(Word word) {
    words.add(word);
  }

  public List<Word> getWords() {
    return words;
  }

}
