package eu.interedition.collatex.superbase;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

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

  public void add(Column column) {
    columns.add(column);
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
      add(new Column(word));
    }
    witnesses.add(w1);
  }

  public String toXML() {
    return "<xml></xml>";
  }

  @Override
  public String toString() {
    String collectedStrings = "";
    for (Witness witness : witnesses) {
      collectedStrings += witness.id + ": ";
      String delim = "";
      for (Column column : columns) {
        collectedStrings += delim + cellToString(witness, column);
        delim = "|";
      }
      collectedStrings += "\n";
    }
    return collectedStrings;
  }

  private String cellToString(Witness witness, Column column) {
    if (!column.containsWitness(witness)) {
      return " ";
    }
    return column.getWord(witness).toString();
  }

  // Note: go through table here or direct through column?
  // columnFor.addMatch(witness, witnessWord);
  // if you go directly the table is not notified about
  // the fact that a witness is added
  public void addMatch(Witness witness, Word word, Column column) {
    column.addMatch(witness, word);
    addWitness(witness);
  }

  private void addWitness(Witness witness) {
    // TODO: an ordered set instead of list would be nice here
    if (!witnesses.contains(witness)) {
      witnesses.add(witness);
    }
  }

  public List<Column> getColumns() {
    return columns;
  }

  public void addVariant(Column column, Witness witness, Word wordInWitness) {
    column.addVariant(witness, wordInWitness);
    addWitness(witness);
  }

  public void addMatchBefore(Column column, Witness witness, Word firstWord) {
    int indexOf = columns.indexOf(column);
    if (indexOf == -1) {
      throw new RuntimeException("Unexpected error: Column not found!");
    }
    Column extraColumn = new Column(firstWord);
    columns.add(indexOf, extraColumn);
    addWitness(witness);
  }

  public void addMatchAtTheEnd(Witness witness, Word firstWord) {
    Column extraColumn = new Column(firstWord);
    columns.add(extraColumn);
    addWitness(witness);
  }
}
