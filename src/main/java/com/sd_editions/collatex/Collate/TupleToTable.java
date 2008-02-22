package com.sd_editions.collatex.Collate;

import java.util.List;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class TupleToTable {

  private Table table;
  private final Line base;
  private final Line witness;
  private final Tuple[] tuples;
  private int variant = 1;
  private int column;

  public TupleToTable(BlockStructure base, BlockStructure variant, Tuple[] tuples) {
    this.tuples = tuples;
    this.base = (Line) base.getRootBlock();
    this.witness = (Line) variant.getRootBlock();
    this.table = new Table(this.base);
    fillTable();
  }

  private void fillTable() {
    int baseIndex = 0;
    int witnessIndex = 0;
    for (int i = 0; i < tuples.length; i++) {
      System.out.println("i: " + i + "; baseIndex: " + baseIndex + "; witnessIndex: " + witnessIndex);
      Tuple tuple = tuples[i];
      int difBaseIndex = tuple.baseIndex - baseIndex;
      int difWitnessIndex = tuple.witnessIndex - witnessIndex;
      if (difBaseIndex > 1 && difWitnessIndex > 1) {
        List<Word> replacementWords = witness.getPhrase(witnessIndex+1, tuple.witnessIndex-1); 
        addReplacementToTable(baseIndex+1, replacementWords);
      } else if (difBaseIndex > 1 && difWitnessIndex == 1) {
        addOmissionToTable(baseIndex+1);
      } else if (difBaseIndex == 1 && difWitnessIndex > 1) {
        List<Word> additionalWords = witness.getPhrase(witnessIndex+1, tuple.witnessIndex-1);
        addFrontAdditionToTable(baseIndex+1, additionalWords);
      }
      baseIndex = tuple.baseIndex;
      witnessIndex = tuple.witnessIndex;
      column = baseIndex * 2 - 2;
      addIdenticalToTable(base.get(baseIndex), witness.get(witnessIndex));
    }
    int difBaseIndex = base.size() - baseIndex;
    int difWitnessIndex = witness.size() - witnessIndex;
    if (difBaseIndex > 0 && difWitnessIndex > 0) {
      addReplacementToTable(baseIndex+1, witness.getPhrase(witnessIndex+1, witness.size()));
    } else if (difBaseIndex > 0 && difWitnessIndex == 0) {
      addOmissionToTable(baseIndex+1);
    } else if (difBaseIndex == 0 && difWitnessIndex > 0) {
      List<Word> additionalWords = witness.getPhrase(witnessIndex+1, witness.size());
      addBackAdditionToTable(baseIndex, additionalWords);
    }
  }

  private void addOmissionToTable(int baseIndex) {
    column = baseIndex * 2 - 2;
    Cell omission = new Omission(base.get(baseIndex));
    addAlignmentInformationToResult(2, omission);
  }

  private void addFrontAdditionToTable(int baseIndex, List<Word> witnessWords) {
    column = baseIndex * 2 - 2;
    Cell addition = new Addition(witnessWords);
    addAlignmentInformationToResult(1, addition);
  }
  
  private void addBackAdditionToTable(int baseIndex, List<Word> witnessWords) {
    column = baseIndex * 2 - 2;
    Cell addition = new Addition(witnessWords);
    addAlignmentInformationToResult(3, addition);
  }

  //  table.setReplacement(1, baseIndex, List<Word> replacements)
  private void addReplacementToTable(int baseIndex, List<Word> replacementWords) {
    column = baseIndex * 2 - 2;
    Cell replacement = new Replacement(base.get(baseIndex), replacementWords);
    addAlignmentInformationToResult(2, replacement);
  }

  private void addIdenticalToTable(Word baseWord, Word witnessWord) {
    Cell alignment;
    if (baseWord.alignmentFactor(witnessWord) == 0) {
      alignment = new AlignmentIdentical(baseWord, witnessWord);
    } else {
      alignment = new AlignmentVariant(baseWord, witnessWord);
    }
    addAlignmentInformationToResult(2, alignment);
  }

  private void addAlignmentInformationToResult(int offset, Cell alignment) {
    table.setCell(variant, column + offset, alignment);
  }

  public Table getTable() {
    return table;
  }

}
