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

package com.sd_editions.collatex.Collate;

import java.util.Collections;
import java.util.List;

import com.sd_editions.collatex.Block.Word;

public class ReplacementCell extends Cell {

  private final Word baseWord;
  private final List<Word> replacementWords;

  public ReplacementCell(Word baseWord, Word replacementWord) {
    this(baseWord, Collections.singletonList(replacementWord));
  }

  public ReplacementCell(Word baseWord, List<Word> replacementWords) {
    this.baseWord = baseWord;
    this.replacementWords = replacementWords;
  }

  @Override
  public String toHTML() {
    return replacementsAsString();
  }

  @Override
  public String toString() {
    return "replacement: " + baseWord.getContent() + " / " + replacementsAsString();
  }

  private String replacementsAsString() {
    StringBuilder replacementString = new StringBuilder();
    String divider = "";
    for (Word replacement : replacementWords) {
      replacementString.append(divider).append(replacement.getContent());
      divider = " ";
    }
    return replacementString.toString();
  }

  @Override
  public String getType() {
    return "replacement";
  }

}
