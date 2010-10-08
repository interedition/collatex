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

import java.util.List;

import com.google.common.collect.Lists;

@Deprecated
public class Line extends Block {

  int lineNumber;

  public Line(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  @Override
  public String toString() {
    return "<l number=\"" + this.getLineNumber() + "\">";
  }

  @Override
  public void accept(IntBlockVisitor visitor) {
    visitor.visitLine(this);
  }

  public Word get(int i) {
    Block child = this.getFirstChild();
    for (int j = 1; j < i; j++) {
      child = child.getNextSibling();
    }
    return (Word) child;
  }

  public int size() {
    if (!this.hasFirstChild()) {
      return 0;
    }
    int i = 1;
    Block child = this.getFirstChild();
    while (child.hasNextSibling()) {
      i++;
      child = child.getNextSibling();
    }
    return i;
  }

  public List<Word> getPhrase(int i, int j) {
    List<Word> result = Lists.newArrayList();
    Word word = get(i);
    result.add(word);
    for (int k = i; k < j; k++) {
      word = (Word) word.getNextSibling();
      result.add(word);
    }
    return result;
  }

}
