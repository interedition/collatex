package com.sd_editions.collatex;

import java.lang.String;
import java.util.HashMap;
/**
 * Our basic block class.
 * This shouldn't be used itself, use one of it's subclasses or create your own
 * Quick check for svn usage
 *
 */
public abstract class Block {

  /* The Block's name */
  private String name;
  /* Any attributes associated with this block */
  private HashMap attributes;
  /* Basic XML like association with other Blocks */
  private Block firstChild;
  private Block lastChild;
  private Block nextSibling;
  private Block previousSibling;
  private Block startParent;
  private Block endParent;

  public Block() {
	this.attributes = new HashMap();
  }

  public String getName() {
	return this.name;
  }

  public void setName(String name) {
	this.name = name;
  }

  public void setFirstChild(Block firstChild) {
	this.firstChild = firstChild;
  }

  public Block getFirstChild() {
	return firstChild;
  }

  public void setLastChild(Block lastChild) {
	this.lastChild = lastChild;
  }

  public Block getLastChild() {
	return this.lastChild;
  }

  public void setNextSibling(Block nextSibling) {
	this.nextSibling = nextSibling;
  }

  public Block getNextSibling() {
	return this.nextSibling;
  }

  public void setPreviousSibling(Block previousSibling) {
	this.previousSibling = previousSibling;
  }

  public Block getPreviousSibling() {
	return this.previousSibling;
  }

  public void setStartParent(Block startParent) {
	this.startParent = startParent;
  }

  public Block getStartParent() {
	return this.startParent;
  }

  public void setEndParent(Block endParent) {
	this.endParent = endParent;
  }

  public Block getEndParent() {
	return this.endParent;
  }
}
