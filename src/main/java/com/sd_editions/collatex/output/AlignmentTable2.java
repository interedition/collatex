package com.sd_editions.collatex.output;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.WitnessBuilder;
import com.sd_editions.collatex.permutations.Word;

// Note: for the TEI xml output it is easier to
// have a Column be a list<phrase>
// However, for building the alignment table it
// is easier to have a Column be a list<word>
public class AlignmentTable2 {
  private final List<Column> columns;

  public AlignmentTable2() {
    this.columns = Lists.newArrayList();
  }

  public void add(DefaultColumn defaultColumn) {
    columns.add(defaultColumn);
  }

  public Witness createSuperBase() {
    WitnessBuilder builder = new WitnessBuilder();
    String collectedStrings = "";
    String delim = "";
    for (Column column : columns) {
      collectedStrings += delim + column.toString();
      delim = " ";
    }
    Witness superWitness = builder.build("superbase", collectedStrings);
    return superWitness;
  }

  void addFirstWitness(Witness w1) {
    for (Word word : w1.getWords()) {
      add(new DefaultColumn(word));
    }
  }

}
