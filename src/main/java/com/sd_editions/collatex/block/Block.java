package com.sd_editions.collatex.Block;

import java.lang.String;
import java.util.HashMap;
/**
 * Our basic block class.
 * This shouldn't be used itself, use one of it's subclasses or create your own
 *
 */
public abstract class Block {

  /* The Block's name */
  private String name;
  /* Any attributes associated with this block */
  private HashMap<String, String> attributes;
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

  protected void setName(String name) {
	this.name = name;
  }

  protected void setFirstChild(Block firstChild) {
	this.firstChild = firstChild;
  }

  public Block getFirstChild() {
	return firstChild;
  }

  public boolean hasFirstChild() {
	if (this.firstChild==null) {
	  return false;
	} else {
	  return true;
	}
  }

  protected void removeFirstChild() {
	this.firstChild = null;
  }

  protected void setLastChild(Block lastChild) {
	this.lastChild = lastChild;
  }

  public Block getLastChild() {
	return this.lastChild;
  }

  public boolean hasLastChild() {
	if (this.lastChild==null) {
	  return false;
	} else {
	  return true;
	}
  }

  protected void removeLastChild() {
	this.lastChild = null;
  }

  protected void setNextSibling(Block nextSibling) {
	this.nextSibling = nextSibling;
  }

  public Block getNextSibling() {
	return this.nextSibling;
  }

  public boolean hasNextSibling() {
	if (this.nextSibling==null) {
	  return false;
	} else {
	  return true;
	}
  }

  protected void removeNextSibling() {
	this.nextSibling = null;
  }

  protected void setPreviousSibling(Block previousSibling) {
	this.previousSibling = previousSibling;
  }

  public Block getPreviousSibling() {
	return this.previousSibling;
  }

  public boolean hasPreviousSibling() {
	if (this.previousSibling==null) {
	  return false;
	} else {
	  return true;
	}
  }

  protected void removePreviousSibling() {
	this.previousSibling = null;
  }

  protected void setStartParent(Block startParent) {
	this.startParent = startParent;
  }

  public Block getStartParent() {
	return this.startParent;
  }

  public boolean hasStartParent() {
	if (this.startParent==null) {
	  return false;
	} else {
	  return true;
	}
  }

  protected void removeStartParent() {
	this.startParent = null;
  }

  protected void setEndParent(Block endParent) {
	this.endParent = endParent;
  }

  public Block getEndParent() {
	return this.endParent;
  }

  public boolean hasEndParent() {
	if (this.endParent==null) {
	  return false;
	} else {
	  return true;
	}
  }

  protected void removeEndParent() {
	this.endParent = null;
  }

  public void setAttribute(String key, String value) {
	this.attributes.put(key, value);
  }

  public String getAttribute(String key) {
	return this.attributes.get(key);
  }

  public String removeAttribute(String key) {
	return this.attributes.remove(key);
  }

  public int numberOfAttributes() {
	return this.attributes.size();
  }

}
