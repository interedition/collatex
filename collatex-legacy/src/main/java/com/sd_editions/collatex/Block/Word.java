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

package com.sd_editions.collatex.Block;

import org.apache.commons.lang.StringUtils;

@Deprecated
public class Word extends Block {
  private String content;

  public Word(String newContent) {
    this.content = newContent;
  }

  public void setContent(String newContent) {
    this.content = newContent;
  }

  public String getContent() {
    return this.content;
  }

  @Override
  public String toString() {
    return "<w>" + this.content + "</w>";
  }

  @Override
  public void accept(IntBlockVisitor visitor) {
    visitor.visitWord(this);
  }

  public boolean alignsWith(Word the_other) {
    return alignmentFactor(the_other) < 2;
  }

  public int alignmentFactor(Word the_other) {
    String left = getContent().toLowerCase();
    String right = the_other.getContent().toLowerCase();
    return StringUtils.getLevenshteinDistance(left, right);
  }

}
