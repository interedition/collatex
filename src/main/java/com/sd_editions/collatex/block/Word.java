package com.sd_editions.collatex.Block;

/**
 *
 *
 *
 *
 *
 *
 *
 */
public class Word extends Block {
  private String content;
  private Word alignedWord;

  public Word(String content) {
    this.content = content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getContent() {
    return this.content;
  }

  public String toString() {
    return "<w>" + this.content + "</w>";
  }

  public void accept(IntBlockVisitor visitor) {
    visitor.visitWord(this);
  }

  public Word getAlignedWord() {
    return alignedWord;
  }

  public void setAlignedWord(Word the_other) {
    this.alignedWord = the_other;
  }

  public boolean alignsWith(Word the_other) {
    return getContent().equalsIgnoreCase(the_other.getContent());
  }

}
