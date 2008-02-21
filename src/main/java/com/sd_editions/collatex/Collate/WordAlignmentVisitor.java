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
  private int baseIndex = 1;
  private int witnessIndex = 1;

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
    Word witnessWord = (Word) witnessBlock;
    System.out.println("visitWord: comparing " + baseWord + " + " + witnessWord);
    if (baseWord.alignsWith(witnessWord)) {
      result.add(new Tuple(baseIndex, witnessIndex));
      baseIndex++;
      witnessIndex++;
      witnessBlock = witnessBlock.getNextSibling();
    } else {
      // now, first try to find a match in the witnessBlock for the baseWord
      Block savedWitnessPosition = witnessBlock;
      int savedWitnessIndex = witnessIndex;
      boolean foundMatchInWitness = false;
      while (!foundMatchInWitness && witnessWord.hasNextSibling()) {
        witnessWord = (Word) witnessWord.getNextSibling();
        witnessIndex++;
        if (baseWord.alignsWith(witnessWord)) {
          result.add(new Tuple(baseIndex, witnessIndex));
          baseIndex++;
          witnessIndex = savedWitnessIndex;
          witnessBlock = witnessWord.getNextSibling();
          foundMatchInWitness = true;
        }
      }
      if (!foundMatchInWitness) {
        // reset
        witnessBlock = savedWitnessPosition;
        witnessWord = (Word) savedWitnessPosition;
        witnessIndex = savedWitnessIndex;
        // now try to find a baseWord that matches the current witnessWord;
        boolean foundMatchInBase = false;
        while (!foundMatchInBase && baseWord.hasNextSibling()) {
          baseWord = (Word) baseWord.getNextSibling();
          baseIndex++;
          if (baseWord.alignsWith(witnessWord)) {
            result.add(new Tuple(baseIndex, witnessIndex));
            witnessIndex++;
            witnessIndex = savedWitnessIndex;
            baseWord = (Word) baseWord.getNextSibling();
            foundMatchInBase = true;
          }
        }
      }
    }

  }

  public Tuple[] getResult() {
    System.out.println("getResult: " + result);
    return result.toArray(new Tuple[] {});
  }

}
