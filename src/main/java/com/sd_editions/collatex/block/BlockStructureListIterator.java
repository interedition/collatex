package com.sd_editions.collatex.Block;

import java.util.ListIterator;
import java.lang.UnsupportedOperationException;
import java.util.NoSuchElementException;

public class BlockStructureListIterator<E> implements ListIterator<E> {

  private BlockStructure bs;
  private Block currentBlock;

  public BlockStructureListIterator(BlockStructure bs) {
	this.bs = bs;
	currentBlock = null;
  }

  /**
   * Not implement at the moment
   */
  public void add(E block) {
	throw new UnsupportedOperationException();
  }

  public boolean hasNext() {
	if (currentBlock==null && bs.getNumberOfBlocks()>0) {
	  return true;
	} else if (currentBlock.hasNextSibling() || currentBlock.hasFirstChild()) {
	  return true;
	} else {
	  return false;
	}
  }

  /*
   *
   * @return true if the BlockStructure has a previous Block
   */
  public boolean hasPrevious() {
	//If the currentBlock is null, check we actually have some
	//blocks in the structure is so then there is a previous!
	if (currentBlock==null && bs.getNumberOfBlocks()>0) {
	  return true;
	} else if (currentBlock.hasPreviousSibling() || currentBlock.hasStartParent()) {
	  return true;
	} else {
	  return false;
	}
  }

  /*
   * Returns the next Block in this BlockStructure.
   *
   * @return The next Block in the BlockStructure
   */
  public E next() {
	if (this.currentBlock==null) {
	  this.currentBlock = this.bs.getRootBlock();
	  if (this.currentBlock==null) { throw new NoSuchElementException(); }
	  return (E) this.currentBlock;
	}
	//If the current element doesn't have a first child or next sibling
	if (!this.currentBlock.hasFirstChild() && !this.currentBlock.hasNextSibling()) {
	  throw new NoSuchElementException();
	}
	this.currentBlock = this.currentBlock.getNextSibling();
	return (E) this.currentBlock;
  }

  /**
   * Not implement at the moment
   */
  public int nextIndex() {
	throw new UnsupportedOperationException();
  }

  /*
   * Returns the previous Block in the BlockStructure.
   *
   * @return The previous block
   */
  public E previous() {
	return null;
  }

  /**
   * Not implement at the moment
   */
  public int previousIndex() {
	throw new UnsupportedOperationException();
  }

  /**
   * Not implement at the moment
   */
  public void remove() {
	throw new UnsupportedOperationException();
  }

  /**
   * Not implement at the moment
   */
  public void set(E block) {
	throw new UnsupportedOperationException();
  }
}
