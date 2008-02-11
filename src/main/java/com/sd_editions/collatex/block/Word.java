package com.sd_editions.collatex.Block;

import java.lang.String;
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

}
