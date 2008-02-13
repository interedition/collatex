package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.IntBlockVisitor;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class TextAlignmentVisitor implements IntBlockVisitor {
  private Block witnessBlock;

  public TextAlignmentVisitor(BlockStructure variant) {
    this.witnessBlock = variant.getRootBlock();
  }

  public TextAlignmentVisitor(Word variant) {
    this.witnessBlock = variant;
  }

  public void visitBlockStructure(BlockStructure blockStructure) {
    Block rootBlock = blockStructure.getRootBlock();
    rootBlock.accept(this);
  }

  public void visitLine(Line line) {
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
    if (witnessWord != null) {
      if (baseWord.alignsWith(witnessWord)) {
        witnessWord.setAlignedWord(baseWord);
        this.witnessBlock = witnessBlock.getNextSibling();

      } else if (baseWord.hasNextSibling() && witnessWord.hasNextSibling()) {
        Word nextBaseWord = (Word) baseWord.getNextSibling();
        Word nextWitnessWord = (Word) witnessWord.getNextSibling();
        if (nextBaseWord.alignsWith(nextWitnessWord)) {
          witnessWord.setAlignedWord(baseWord);
          this.witnessBlock = witnessBlock.getNextSibling();
        } else if (baseWord.alignsWith(nextWitnessWord)) {
          nextWitnessWord.setAlignedWord(baseWord);
          this.witnessBlock = nextWitnessWord.getNextSibling();
        }

      } else if (!baseWord.hasNextSibling() && witnessWord.hasNextSibling()) {
        Word nextWitnessWord = (Word) witnessWord.getNextSibling();
        if (baseWord.alignsWith(nextWitnessWord)) {
          nextWitnessWord.setAlignedWord(baseWord);
          this.witnessBlock = nextWitnessWord.getNextSibling();
        }
      }
    }
  }

}
