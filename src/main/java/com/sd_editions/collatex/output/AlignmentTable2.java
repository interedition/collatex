package com.sd_editions.collatex.output;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.Superbase;
import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.Word;

// Note: for the TEI xml output it is easier to
// have a Column be a list<phrase>
// However, for building the alignment table it
// is easier to have a Column be a list<word>
public class AlignmentTable2 {
  private final List<Column> columns;
  private final List<Witness> witnesses;

  public AlignmentTable2() {
    this.columns = Lists.newArrayList();
    this.witnesses = Lists.newArrayList();
  }

  public void add(DefaultColumn defaultColumn) {
    columns.add(defaultColumn);
  }

  public Superbase createSuperbase() {
    Superbase superbase = new Superbase();
    for (Column column : columns) {
      // Note: for now we pretend every column is a Match column
      // so we only take the first word for the superbase
      Word matchWord = column.getWords().iterator().next();
      superbase.addWord(matchWord, column);
    }
    return superbase;
  }

  //  public Witness createSuperBase() {
  //    WitnessBuilder builder = new WitnessBuilder();
  //    String collectedStrings = "";
  //    String delim = "";
  //    for (Column column : columns) {
  //      collectedStrings += delim + column.toString();
  //      delim = " ";
  //    }
  //    Witness superWitness = builder.build("superbase", collectedStrings);
  //    return superWitness;
  //  }

  void addFirstWitness(Witness w1) {
    for (Word word : w1.getWords()) {
      add(new DefaultColumn(word));
    }
    witnesses.add(w1);
  }

  public String toXML() {
    return "<xml></xml>";
  }

  @Override
  public String toString() {
    String collectedStrings = "";
    String linebreak = "";
    for (Witness witness : witnesses) {
      collectedStrings += linebreak + witness.id + ": ";
      linebreak = "\n";
      String delim = "";
      for (Column column : columns) {
        collectedStrings += delim + cellToString(witness, column);
        delim = "|";
      }
    }
    return collectedStrings;
  }

  private String cellToString(Witness witness, Column column) {
    if (!column.containsWitness(witness)) {
      return " ";
    }
    return column.getWord(witness).toString();
  }

  public void addMatch(Witness w2, Word wordOnPosition) {
    Column column = columns.get(wordOnPosition.position - 1);
    column.addMatch(w2, wordOnPosition);
    // TODO: an ordered set instead of list would be nice here
    if (!witnesses.contains(w2)) {
      witnesses.add(w2);
    }
  }
}
