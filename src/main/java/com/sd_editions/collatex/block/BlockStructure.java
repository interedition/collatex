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
	if (rootBlock!=null) {
	  throw new BlockStructureCascadeException();
	  return;
	}
	setRootBlock(root, false);
  }

  /**
   * Set the root Block for this structure
   *
   * @param root The root block for this structure
   * @param cascade If true then this will remove all blocks already in the structure if any exist, default's to false
   *
   * @throws BlockStructureCascadeException thrown if the structure already contains a root element and 
   *		 cascade is set to false
   *
   */
  public void setRootBlock(Block root, boolean cascade) throws BlockStructureCascadeException {
	if (rootBlock==null) {
	  this.rootBlock = root;
	} else if (cascade) {
	  //We're o.k. to remove all the containing blocks
	  blocks.clear();
	} else {
	  throw new BlockStructureCascadeException();
	}
  }

  /**
   * Set's the child block
   *
   * @param parent The parent Block to set a child of
   * @param child The child Block
   *
   * @throws BlockStructureCascadeException thrown if the parent Block already contains a child element and
   */
  public void setChildBlock(Block parent, Block child) {
	setChildBlock(parent, child, false);
  }

  /**
   * Set's the child block 
   *
   * @param parent The parent block to set a child of
   * @param child The child block
   * @param cascade If true then this will remove all blocks already in the structure if any exist, default's to false
   *
   * @throws BlockStructureCascadeException thrown if the parent Block already contains a child element and
   *		 cascade is set to false
   */
  public void setChildBlock(Block parent, Block child, boolean cascade) {
	child.setStartParent(parent);
	child.setEndParent(parent);
  }

}
