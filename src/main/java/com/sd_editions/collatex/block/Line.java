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
  
  public void accept(IntBlockVisitor visitor) {
	  visitor.visitLine(this);
  }

	public Block get(int i) {
		Block child = this.getFirstChild();
		for (int j = 1; j<i; j++) {
			child = child.getNextSibling();
		}
		return child;
	}

}
