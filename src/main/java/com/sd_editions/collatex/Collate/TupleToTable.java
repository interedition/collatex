package com.sd_editions.collatex.Collate;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class TupleToTable {

  private Table table;
  private Line base = null;
  private final Tuple[][] tupleMatrix;
  private final List<BlockStructure> witnessList;

  public TupleToTable(BlockStructure newBase, BlockStructure witness, Tuple[] newTupleArray) {
    this.tupleMatrix = new Tuple[][] { newTupleArray };
    this.witnessList = Lists.newArrayList();
    this.witnessList.add(witness);
    construct(newBase);
  }

  public TupleToTable(BlockStructure newBase, List<BlockStructure> newWitnessList, Tuple[][] newTupleMatrix) {
    this.tupleMatrix = newTupleMatrix;
    this.witnessList = newWitnessList;
    construct(newBase);
  }

  void construct(BlockStructure newBase) {
    this.base = (Line) newBase.getRootBlock();
    this.table = new Table(this.base);
    fillTable();
  }

  private void fillTable() {
    for (int i = 0; i < tupleMatrix.length; i++) {
      int currentBaseIndex = 0;
      int currentWitnessIndex = 0;
      Tuple[] tuples = tupleMatrix[i];
      int witnessNumber = i + 1;
      Line witness = (Line) witnessList.get(i).getRootBlock();
      for (Tuple tuple : tuples) {
        System.out.println("baseIndex: " + currentBaseIndex + "; witnessIndex: " + currentWitnessIndex);
        int baseIndexDif = tuple.baseIndex - currentBaseIndex;
        int witnessIndexDif = tuple.witnessIndex - currentWitnessIndex;
        if (baseIndexDif > 1 && witnessIndexDif > 1) {
          List<Word> replacementWords = witness.getPhrase(currentWitnessIndex + 1, tuple.witnessIndex - 1);
          table.setReplacement(witnessNumber, currentBaseIndex + 1, replacementWords);
        } else if (baseIndexDif > 1 && witnessIndexDif == 1) {
          table.setOmission(witnessNumber, currentBaseIndex + 1, tuple.baseIndex);
        } else if (baseIndexDif == 1 && witnessIndexDif > 1) {
          List<Word> additionalWords = witness.getPhrase(currentWitnessIndex + 1, tuple.witnessIndex - 1);
          table.setFrontAddition(witnessNumber, currentBaseIndex + 1, additionalWords);
        }
        currentBaseIndex = tuple.baseIndex;
        currentWitnessIndex = tuple.witnessIndex;
        table.setIdenticalOrVariant(witnessNumber, currentBaseIndex, witness.get(currentWitnessIndex), tuple);
      }

      int baseIndexDif = base.size() - currentBaseIndex;
      int witnessIndexDif = witness.size() - currentWitnessIndex;
      if (baseIndexDif > 0 && witnessIndexDif > 0) {
        table.setReplacement(witnessNumber, currentBaseIndex + 1, witness.getPhrase(currentWitnessIndex + 1, witness.size()));
      } else if (baseIndexDif > 0 && witnessIndexDif == 0) {
        table.setOmission(witnessNumber, currentBaseIndex + 1, base.size() + 1);
      } else if (baseIndexDif == 0 && witnessIndexDif > 0) {
        List<Word> additionalWords = witness.getPhrase(currentWitnessIndex + 1, witness.size());
        table.setBackAddition(witnessNumber, currentBaseIndex, additionalWords);
      }
    }
  }

  public Table getTable() {
    return table;
  }

}
