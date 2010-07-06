/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.Block;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class BlockStructureListIterator<E> implements ListIterator<E> {

  private final BlockStructure bs;
  private Block previousBlock;
  private Block nextBlock;

  public BlockStructureListIterator(BlockStructure bs) {
    this.bs = bs;
    this.previousBlock = null;
    this.nextBlock = this.bs.getRootBlock();
  }

  /**
   * Not implement at the moment
   */
  public void add(E block) {
    throw new UnsupportedOperationException();
  }

  public boolean hasNext() {
    return !(nextBlock == null || bs.getNumberOfBlocks() == 0);
  }

  /*
   *
   * @return true if the BlockStructure has a previous Block
   */
  public boolean hasPrevious() {
    //Current block actually represents the next block in the iteration, so we need to set back
    return (this.previousBlock != null);
  }

  /*
   * Returns the next Block in this BlockStructure.
   *
   * @return The next Block in the BlockStructure
   */
  @SuppressWarnings("unchecked")
  public E next() {
    if (this.nextBlock == null) {
      throw new NoSuchElementException();
    }
    Block result = this.nextBlock;
    this.previousBlock = this.getPreviousBlock(this.nextBlock);
    if (this.nextBlock.hasFirstChild()) {
      this.nextBlock = this.nextBlock.getFirstChild();
    } else if (this.nextBlock.hasNextSibling()) {
      this.nextBlock = this.nextBlock.getNextSibling();
    } else {
      this.nextBlock = null;
    }
    return (E) result;
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
  @SuppressWarnings("unchecked")
  public E previous() {
    //Right the previous block is the previous previous of the next block
    Block pBlock = this.getPreviousBlock(this.nextBlock);
    Block ppBlock = this.getPreviousBlock(pBlock);
    this.nextBlock = pBlock;
    this.previousBlock = this.getPreviousBlock(ppBlock);
    this.nextBlock = pBlock;
    return (E) ppBlock;
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

  /**
   * Get previous block
   */
  private Block getPreviousBlock(Block thisBlock) {
    if (thisBlock == null) {
      return null;
    } else if (thisBlock.hasPreviousSibling()) {
      return thisBlock.getPreviousSibling();
    } else if (thisBlock.hasStartParent()) {
      return thisBlock.getStartParent();
    } else {
      return null;
    }
  }

  //  /**
  //   * Get a next block
  //   */
  //  private Block getNextBlock(Block thisBlock) {
  //    if (thisBlock == null) {
  //      return null;
  //    } else if (thisBlock.hasNextSibling()) {
  //      return thisBlock.getNextSibling();
  //    } else if (thisBlock.hasEndParent()) {
  //      Block eParent = thisBlock.getEndParent();
  //      if (eParent.hasNextSibling()) {
  //        return eParent.getNextSibling();
  //      }
  //      return null;
  //    } else {
  //      return null;
  //    }
  //  }

  /**
   * toString method, for debugging only.
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("nextBlock = " + this.nextBlock + "\n");
    sb.append("previousBlock = " + this.previousBlock);
    return sb.toString();
  }

}
