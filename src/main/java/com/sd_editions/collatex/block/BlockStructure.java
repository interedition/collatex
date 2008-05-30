package com.sd_editions.collatex.Block;

import java.util.HashSet;

import com.google.common.collect.Sets;

/**
 * Maintains an association between Blocks.
 *
 */
public class BlockStructure {
  HashSet<Block> blocks;
  Block rootBlock;

  public BlockStructure() {
    this.blocks = Sets.newHashSet();
  }

  /**
   * Returns the number of Blocks this BlockStructure contains
   *
   * @return the number of Blocks this BlockStructure contains
   */
  public int getNumberOfBlocks() {
    return this.blocks.size();
  }

  /**
   * Returns the root block for this BlockStructure
   *
   * @return The root block of the structure
   */
  public Block getRootBlock() {
    return this.rootBlock;
  }

  /**
   * @param root The root Block for this structure
   *
   * @throws BlockStructureCascadeException thrown if the structure already contains a root element
   */
  public void setRootBlock(Block root) throws BlockStructureCascadeException {
    setRootBlock(root, false);
  }

  /**
   * Set the root Block for this structure
   *
   * @param root The root Block for this structure
   * @param cascade If true then this will remove all Blocks already in the structure if any exist, default's to false
   *
   * @throws BlockStructureCascadeException thrown if the structure already contains a root element and
   *		 cascade is set to false
   *
   */
  public void setRootBlock(Block root, boolean cascade) throws BlockStructureCascadeException {
    if (rootBlock == null) {
      this.rootBlock = root;
      this.blocks.add(rootBlock);
    } else if (cascade) {
      //We're o.k. to remove all the containing Blocks
      blocks.clear();
    } else {
      throw new BlockStructureCascadeException();
    }
  }

  /**
   * Set's the child Block
   * If the parent Block already has a child Block, simple add this to the next sibling of last child
   *
   * @param parent The parent Block to set a child of
   * @param child The child Block
   *
   */
  public void setChildBlock(Block parent, Block child) {
    child.setStartParent(parent);
    child.setEndParent(parent);
    if (!parent.hasFirstChild()) {
      parent.setFirstChild(child);
      parent.setLastChild(child);
    } else {
      //Need to set the next sibling of the last child to this,
      //set the left sibling to that of the previous last child,
      //the right sibling of the previous last child
      child.setPreviousSibling(parent.getLastChild());
      child.getPreviousSibling().setNextSibling(child);
      parent.setLastChild(child);
    }
    this.blocks.add(child);
  }

  /**
   * Set's the next sibling of a Block
   *
   * If the pBlock already has a next sibling the nBlock will be inserted in between the pBlock and the pBlock's exsiting
   * next sibling
   *
   * @param pBlock The Block that have its next sibling set
   * @param nBlock The Block to set as the next sibling
   *
   */
  public void setNextSibling(Block pBlock, Block nBlock) {
    if (pBlock.hasNextSibling()) {
      //Set the pBlock's next sibling's previous sibling to nBlock
      pBlock.getNextSibling().setPreviousSibling(nBlock);
      nBlock.setNextSibling(pBlock.getNextSibling());
      pBlock.setNextSibling(nBlock);
      nBlock.setPreviousSibling(pBlock);
    } else {
      pBlock.setNextSibling(nBlock);
      nBlock.setPreviousSibling(pBlock);
      nBlock.removeNextSibling();
      //pBlock must be the last child of the parent block, so correct that
      if (pBlock.hasEndParent()) {
        pBlock.getEndParent().setLastChild(nBlock);
      }
    }
    //Either way the nBlock will have the same start/end parent as the pBlock's end parent
    if (pBlock.hasEndParent()) {
      nBlock.setStartParent(pBlock.getEndParent());
      nBlock.setEndParent(pBlock.getEndParent());
    }
  }

  /**
   * Set's the previous sibling of a Block
   *
   * If nBlock already has a previous sibling the pBlock will be insert in between the nBlock and the pBlock's existing
   * previous sibling
   *
   * @param nBlock The Block that will have its previous sibling set
   * @param pBlock The Block to set as the previous sibling
   *
   */
  public void setPreviousSibling(Block nBlock, Block pBlock) {
    if (nBlock.hasPreviousSibling()) {
      //Set the nBlock's previous sibling's next sibling to pBlock
      nBlock.getPreviousSibling().setNextSibling(pBlock);
      pBlock.setPreviousSibling(nBlock.getPreviousSibling());
      nBlock.setPreviousSibling(pBlock);
      pBlock.setNextSibling(nBlock);
    } else {
      nBlock.setPreviousSibling(pBlock);
      pBlock.setNextSibling(nBlock);
      pBlock.removePreviousSibling();
      //nBlock must be the first child of the parent block
      nBlock.getStartParent().setFirstChild(pBlock);
    }
    //Either way the pBlock will have the same start/end parent as the nBlock's start parent
    pBlock.setStartParent(nBlock.getStartParent());
    pBlock.setEndParent(nBlock.getEndParent());
  }

  /**
   * Remove's the Block from the structure, throws a BlockStructureCascadeException if the Block has any children
   *
   * @param block The Block to remove
   *
   */
  public void removeBlock(Block block) throws BlockStructureCascadeException {
    this.removeBlock(block, false);
  }

  /**
   * Remove's a Block from the structure, removing any child Blocks if cascade is set to true
   *
   * @param block The Block to remove
   * @param cascade If true any child Blocks of Block will be remove as well, otherwise a BlockStructureCascadeException will be thrown if the Block contains any children
   *
   */
  public void removeBlock(Block block, boolean cascade) throws BlockStructureCascadeException {
    if (!cascade && (block.hasFirstChild() || block.hasLastChild())) {
      throw new BlockStructureCascadeException();
    } else if (cascade && block.hasFirstChild()) {
      //Loop through the children
      removeBlock(block.getFirstChild());
    }
    //Remove from between any siblings it has
    if (block.hasNextSibling() && block.hasPreviousSibling()) {
      block.getNextSibling().setPreviousSibling(block.getPreviousSibling());
      block.getPreviousSibling().setNextSibling(block.getNextSibling());
    } else if (block.hasNextSibling()) {
      //It's the first block of the parent block
      block.getNextSibling().removePreviousSibling();
      block.getStartParent().setFirstChild(block.getNextSibling());
    } else {
      //It's the last block of the parent block
      if (block.hasPreviousSibling()) {
        block.getPreviousSibling().removeNextSibling();
      }
      if (block.hasEndParent()) {
        block.getEndParent().setLastChild(block.getPreviousSibling());
      }
    }
    //Clear this blocks associations
    block.removeStartParent();
    block.removeEndParent();
    block.removePreviousSibling();
    block.removeNextSibling();
    block.removeFirstChild();
    block.removeLastChild();
    if (this.rootBlock == block) {
      this.rootBlock = null;
    }
    this.blocks.remove(block);
  }

  /**
   * Returns a ListIterator that will iterate over this BlockStructure in an ordered manner.
   *
   * @return BlockStructureListIterator
   */
  @SuppressWarnings("unchecked")
  public BlockStructureListIterator<? extends Block> listIterator() {
    return new BlockStructureListIterator(this);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    //Loop through all the blocks
    Block block = this.rootBlock;
    while (block != null) {
      sb.append(block);
      sb.append("--");
      if (block.hasFirstChild()) {
        Block cBlock = block.getFirstChild();
        while (cBlock.hasNextSibling()) {
          sb.append(cBlock);
          sb.append("--");
          cBlock = cBlock.getNextSibling();
        }
        sb.append(cBlock);
        sb.append("--");
      }
      block = block.getNextSibling();
    }
    return sb.toString();
  }

  public void accept(IntBlockVisitor visitor) {
    visitor.visitBlockStructure(this);
  }
}
