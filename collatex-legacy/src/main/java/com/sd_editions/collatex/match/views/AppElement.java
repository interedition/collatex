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

package com.sd_editions.collatex.match.views;

import eu.interedition.collatex.experimental.ngrams.NGram;

public class AppElement extends Element {
  // TODO rename!
  private final NGram addedWords;
  private final NGram reading;

  public AppElement(final NGram addedWords2) {
    this.addedWords = addedWords2;
    this.reading = null;
  }

  public AppElement(final NGram lemma, final NGram reading2) {
    this.addedWords = lemma;
    this.reading = reading2;
  }

  // TODO use StringBuilder!
  @Override
  public String toXML() {
    String result = "<app>";
    if (reading == null) {
      //TODO should not be getNormalized!
      result += addedWords.getNormalized();
    } else {
      //TODO should not be getNormalized!
      result += "<lemma>" + addedWords.getNormalized() + "</lemma>";
      //TODO should not be getNormalized!
      result += "<reading>" + reading.getNormalized() + "</reading>";
    }
    result += "</app>";
    return result;
  }
}
