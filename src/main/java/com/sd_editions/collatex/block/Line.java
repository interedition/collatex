package com.sd_editions.collatex.Block;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 *
 *
 *
 *
 *
 */
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

  public List<Word> getPhrase(int i, int length) {
    List<Word> result = new ArrayList<Word>();
    Word word = get(i);
    result.add(word);
    for (int k = 0; k < length -1 ; k++) {
      word = (Word) word.getNextSibling();
      result.add(word);
    }
    return result;
  }

}
