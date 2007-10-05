package com.sd_editions.collatex.block;

import java.lang.String;
import java.util.HashMap;
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

}
