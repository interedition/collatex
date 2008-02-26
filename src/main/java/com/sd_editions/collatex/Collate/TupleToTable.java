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
  private int witnessNr = 1;

  public TupleToTable(BlockStructure base1, BlockStructure variant, Tuple[] tuples1) {
    this.tuples = tuples1;
    this.base = (Line) base1.getRootBlock();
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
        List<Word> replacementWords = witness.getPhrase(witnessIndex + 1, tuple.witnessIndex - 1);
        table.setReplacement(witnessNr, baseIndex + 1, replacementWords);
      } else if (difBaseIndex > 1 && difWitnessIndex == 1) {
        table.setOmission(witnessNr, baseIndex + 1, tuple.baseIndex);
      } else if (difBaseIndex == 1 && difWitnessIndex > 1) {
        List<Word> additionalWords = witness.getPhrase(witnessIndex + 1, tuple.witnessIndex - 1);
        table.setFrontAddition(witnessNr, baseIndex + 1, additionalWords);
      }
      baseIndex = tuple.baseIndex;
      witnessIndex = tuple.witnessIndex;
      table.setIdenticalOrVariant(witnessNr, baseIndex, witness.get(witnessIndex));
    }
    int difBaseIndex = base.size() - baseIndex;
    int difWitnessIndex = witness.size() - witnessIndex;
    if (difBaseIndex > 0 && difWitnessIndex > 0) {
      table.setReplacement(witnessNr, baseIndex + 1, witness.getPhrase(witnessIndex + 1, witness.size()));
    } else if (difBaseIndex > 0 && difWitnessIndex == 0) {
      table.setOmission(witnessNr, baseIndex + 1, base.size()+1);
    } else if (difBaseIndex == 0 && difWitnessIndex > 0) {
      List<Word> additionalWords = witness.getPhrase(witnessIndex + 1, witness.size());
      table.setBackAddition(witnessNr, baseIndex, additionalWords);
    }
  }

  public Table getTable() {
    return table;
  }

}
