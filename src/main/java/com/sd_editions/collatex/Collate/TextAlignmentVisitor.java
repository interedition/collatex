package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Block.IntBlockVisitor;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class TextAlignmentVisitor implements IntBlockVisitor {
  private Block witnessBlock;
  private BlockStructure result;
	private Table pTable;
	private int column;
	private int variant = 1;
  
  public TextAlignmentVisitor(BlockStructure variant) {
    this.witnessBlock = variant.getRootBlock();
    this.result = new BlockStructure();
  }

  public TextAlignmentVisitor(Word variant) {
    this.witnessBlock = variant;
    this.result = new BlockStructure();
    createNewTableInResult();
  }

	public void visitBlockStructure(BlockStructure blockStructure) {
    Block rootBlock = blockStructure.getRootBlock();
    rootBlock.accept(this);
  }

  public void visitLine(Line line) {
  	createNewTableInResult();
    this.witnessBlock = witnessBlock.getFirstChild();
    Word w = (Word) line.getFirstChild();
    column = 0;
    w.accept(this);
    while (w.hasNextSibling()) {
      w = (Word) w.getNextSibling();
      column += 2;
      w.accept(this);
    }
    // TODO: move to next line etc...
  }

  public void visitWord(Word baseWord) {
    Word witnessWord = (Word) witnessBlock;
    if (checkOmission(baseWord)) return;
    if (check1(baseWord, witnessWord)) return; 
    if (check3(baseWord, witnessWord)) return;
    if (check4Omission(baseWord, witnessWord)) return;
    if (check5Addition(baseWord, witnessWord)) return; 
   	Cell nonAlignment = new NonAlignment(baseWord, witnessWord);
   	addAlignmentInformationToResult(2, nonAlignment);
  }

	public BlockStructure getResult() {
		return result;
	}

	private boolean checkOmission(Word baseWord) {
		if (witnessBlock == null) {
			Cell alignment = new Omission(baseWord);
			addAlignmentInformationToResult(2, alignment);
			return true;
    }
		return false;
	}

	private boolean check1(Word baseWord, Word witnessWord) {
		if (witnessWord!=null&&baseWord.alignsWith(witnessWord)) {
			addDoAlign(baseWord, witnessWord);
      if (!baseWord.hasNextSibling() && witnessWord.hasNextSibling()) {
  			Cell alignment = new Addition((Word)witnessWord.getNextSibling());
  			addAlignmentInformationToResult(3, alignment);
      	// TODO: we should loop here: there could be more additional words in the witness
  			return true;
      }
      this.witnessBlock = witnessBlock.getNextSibling();
      return true;
    }
		return false;
	}

	private boolean check3(Word baseWord, Word witnessWord) {
		if (baseWord.hasNextSibling() && witnessWord.hasNextSibling()) {
      Word nextWord = (Word) baseWord.getNextSibling();
      Word nextWitnessWord = (Word) witnessWord.getNextSibling();
      if (nextWord.alignsWith(nextWitnessWord)) {
  			Cell alignment = new AlignmentVariant(baseWord, witnessWord);
  			addAlignmentInformationToResult(2, alignment);
        this.witnessBlock = witnessBlock.getNextSibling();
        return true;
      }

    }
		return false;
	}

	private boolean check4Omission(Word baseWord, Word witnessWord) {
		if (baseWord.hasNextSibling() && witnessWord.alignsWith((Word)baseWord.getNextSibling())) {
			Cell alignment = new Omission(baseWord);
			addAlignmentInformationToResult(2, alignment);
			return true;
		}
		return false;
	}

	private boolean check5Addition(Word baseWord, Word witnessWord) {
		if (witnessWord!=null && witnessWord.hasNextSibling()) {
      Word nextWitnessWord = (Word) witnessWord.getNextSibling();
      if (baseWord.alignsWith(nextWitnessWord)) {
  			Cell alignment = new Addition(witnessWord);
  			addAlignmentInformationToResult(1, alignment);
  			addDoAlign(baseWord, nextWitnessWord);
        this.witnessBlock = nextWitnessWord.getNextSibling();
        return true;
      }
    }
		return false;
	}

	private void addDoAlign(Word baseWord, Word witnessWord) {
		Cell alignment;
		if (baseWord.alignmentFactor(witnessWord)==0) {
			alignment = new AlignmentIdentical(baseWord, witnessWord);			
		} else {
			alignment = new AlignmentVariant(baseWord, witnessWord);
		}
		addAlignmentInformationToResult(2, alignment);
	}

  private void createNewTableInResult() {
		Table nTable = new Table();
		if (pTable == null) {
			try {
				result.setRootBlock(nTable, true);
			} catch (BlockStructureCascadeException e) {
				throw new RuntimeException(e);
			}
		} else {
			result.setNextSibling(pTable, nTable);
		}
		pTable = nTable;
	}

	private void addAlignmentInformationToResult(int offset, Cell alignment) {
		pTable.setCell(variant, column+offset, alignment);
	}

}
