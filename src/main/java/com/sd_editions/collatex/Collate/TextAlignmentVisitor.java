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
	private int lineCount;
	private Block pLine;
  
  public TextAlignmentVisitor(BlockStructure variant) {
    this.witnessBlock = variant.getRootBlock();
    this.result = new BlockStructure();
  }

  public TextAlignmentVisitor(Word variant) {
    this.witnessBlock = variant;
    this.result = new BlockStructure();
    createNewLineInResult();
  }

  public void visitBlockStructure(BlockStructure blockStructure) {
    Block rootBlock = blockStructure.getRootBlock();
    rootBlock.accept(this);
  }

  public void visitLine(Line line) {
		createNewLineInResult();
    this.witnessBlock = witnessBlock.getFirstChild();
    Word w = (Word) line.getFirstChild();
    w.accept(this);
    while (w.hasNextSibling()) {
      w = (Word) w.getNextSibling();
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
   	Block nonAlignment = new NonAlignment(baseWord, witnessWord);
   	addAlignmentInformationToResult(nonAlignment);
  }

	public BlockStructure getResult() {
		return result;
	}

	private boolean checkOmission(Word baseWord) {
		if (witnessBlock == null) {
			Block alignment = new Omission(baseWord);
			addAlignmentInformationToResult(alignment);
			return true;
    }
		return false;
	}

	private boolean check1(Word baseWord, Word witnessWord) {
		if (witnessWord!=null&&baseWord.alignsWith(witnessWord)) {
			Block alignment = new AlignmentIdentical(baseWord, witnessWord);
			addAlignmentInformationToResult(alignment);
      if (!baseWord.hasNextSibling() && witnessWord.hasNextSibling()) {
  			alignment = new Addition((Word)witnessWord.getNextSibling());
  			addAlignmentInformationToResult(alignment);
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
  			Block alignment = new AlignmentVariant(baseWord, witnessWord);
  			addAlignmentInformationToResult(alignment);
        this.witnessBlock = witnessBlock.getNextSibling();
        return true;
      }

    }
		return false;
	}

	private boolean check4Omission(Word baseWord, Word witnessWord) {
		if (baseWord.hasNextSibling() && witnessWord.alignsWith((Word)baseWord.getNextSibling())) {
			Block alignment = new Omission(baseWord);
			addAlignmentInformationToResult(alignment);
			return true;
		}
		return false;
	}

	private boolean check5Addition(Word baseWord, Word witnessWord) {
		if (witnessWord!=null && witnessWord.hasNextSibling()) {
      Word nextWitnessWord = (Word) witnessWord.getNextSibling();
      if (baseWord.alignsWith(nextWitnessWord)) {
  			Block alignment = new Addition(witnessWord);
  			addAlignmentInformationToResult(alignment);
  			alignment = new AlignmentIdentical(baseWord, nextWitnessWord);
  			addAlignmentInformationToResult(alignment);
        this.witnessBlock = nextWitnessWord.getNextSibling();
        return true;
      }

    }
		return false;
	}

	private void createNewLineInResult() {
		lineCount++;
		Line nLine = new Line(lineCount);
		if (pLine == null) {
			try {
				result.setRootBlock(nLine, true);
			} catch (BlockStructureCascadeException e) {
				throw new RuntimeException(e);
			}
		} else {
			result.setNextSibling(pLine, nLine);
		}
		pLine = nLine;
	}

	private void addAlignmentInformationToResult(Block alignment) {
		result.setChildBlock(pLine, alignment);
	}

}
