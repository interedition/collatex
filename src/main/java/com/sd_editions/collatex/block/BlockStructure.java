package com.sd_editions.collatex.block;

import java.util.HashSet;

/**
 * Maintains an association between Blocks.
 *
 */
public class BlockStructure {
  HashSet blocks;
  Block rootBlock;

  public BlockStructure() {
	this.blocks = new HashSet();
  }

  /**
   * @param root The root block for this structure
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
	if (rootBlock==null) {
	  this.rootBlock = root;
	} else if (cascade) {
	  //We're o.k. to remove all the containing Blocks
	  blocks.clear();
	} else {
	  throw new BlockStructureCascadeException();
	}
  }

  /**
   * Set's the child block
   * If the parent Block already has a child block, simple add this to the next sibling of last child
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
	} else  {
	  //Need to set the next sibling of the last child to this,
	  //set the left sibling to that of the previous last child,
	  //the right sibling of the previous last child
	  child.setPreviousSibling(parent.getLastChild());
	  child.getPreviousSibling().setNextSibling(child);
	}
  }

}
