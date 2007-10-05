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

  public String toString() {
	return "<l number=\"" + this.getLineNumber()+ "\">";
  }

}
