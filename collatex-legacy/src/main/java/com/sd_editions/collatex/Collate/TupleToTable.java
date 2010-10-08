/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
