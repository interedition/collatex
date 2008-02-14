package com.sd_editions.collatex.Block;

import org.apache.commons.lang.StringUtils;

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

  public boolean alignsWith(Word the_other) {
  	return alignmentFactor(the_other) < 2;
  }
  
  public int alignmentFactor(Word the_other) {
  	String left = getContent().toLowerCase();
  	String right = the_other.getContent().toLowerCase();
    return StringUtils.getLevenshteinDistance(left, right);  	
  }

}
