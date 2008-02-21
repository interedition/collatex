package com.sd_editions.collatex.Collate;

import java.util.ArrayList;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.IntBlockVisitor;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class WordAlignmentVisitor implements IntBlockVisitor {
  private Block witnessBlock;
  private ArrayList<Tuple> result;
  private int baseIndex=1;
  private int witnessIndex=1;

  public WordAlignmentVisitor(BlockStructure variant) {
    this.witnessBlock = variant.getRootBlock();
  }

  public WordAlignmentVisitor(Word variant) {
    this.witnessBlock = variant;
    this.result = new ArrayList<Tuple>();
    createResult();
  }

  @SuppressWarnings("serial")
  private void createResult() {
    result = new ArrayList<Tuple>() {};
  }

  public void visitBlockStructure(BlockStructure blockStructure) {
    Block rootBlock = blockStructure.getRootBlock();
    rootBlock.accept(this);
  }

  public void visitLine(Line line) {
    createResult();
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
    System.out.println("visitWord("+baseWord+")");
    Word witnessWord = (Word) witnessBlock;
    if (baseWord.alignmentFactor(witnessWord) == 0) {
      result.add(new Tuple(baseIndex, witnessIndex));
    }
    baseIndex++;
    witnessIndex++;
  }

  public Tuple[] getResult() {
    return result.toArray(new Tuple[] {});
  }

}
