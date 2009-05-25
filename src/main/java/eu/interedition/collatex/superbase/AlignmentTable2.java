package eu.interedition.collatex.superbase;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.collation.Gap;
import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.collation.MatchNonMatch;
import eu.interedition.collatex.collation.NonMatch;
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
      column.addToSuperbase(superbase);
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
      add(new Column(w1, word));
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
    addWitnessToInternalList(witness);
  }

  private void addWitnessToInternalList(Witness witness) {
    // TODO: an ordered set instead of list would be nice here
    if (!witnesses.contains(witness)) {
      witnesses.add(witness);
    }
  }

  public void addWitness(Witness witness) {
    // make the superbase from the alignment table
    Superbase superbase = createSuperbase();
    CollateCore core = new CollateCore();
    MatchNonMatch compresult = core.compareWitnesses(superbase, witness);

    Set<Match> matches = compresult.getMatches();
    for (Match match : matches) {
      Word baseWord = match.getBaseWord();
      Column column = superbase.getColumnFor(baseWord);
      Word witnessWord = match.getWitnessWord();
      addMatch(witness, witnessWord, column);
    }

    List<NonMatch> replacements = compresult.getReplacements();
    for (NonMatch replacement : replacements) {
      // TODO: hou rekening met langere additions!

      Iterator<Word> baseIterator = replacement.getBase().getWords().iterator();
      Iterator<Word> witnessIterator = replacement.getWitness().getWords().iterator();
      while (baseIterator.hasNext()) {
        Word wordInOriginal = baseIterator.next();
        Column column = superbase.getColumnFor(wordInOriginal);
        if (witnessIterator.hasNext()) {
          Word wordInWitness = witnessIterator.next();
          if (column.containsWitness(witness)) { // already have something in here from the matches phase
            addVariantBefore(column, witness, Lists.newArrayList(wordInWitness)); // FIXME but this doesn't handle longer sequences ...
          } else {
            addVariant(column, witness, wordInWitness);
          }
        }
      }
      // still have words in the witness? add new columns after the last one from the base
      if (witnessIterator.hasNext()) {
        LinkedList<Word> remainingWitnessWords = Lists.newLinkedList(witnessIterator);
        addVariantAtGap(superbase, witness, replacement.getBase(), remainingWitnessWords);
      }

      //      Word wordInOriginal = replacement.getBase().getFirstWord();
      //      Word wordInWitness = replacement.getWitness().getFirstWord(); // if witness is longer -> extra columns
      //      Column column = superbase.getColumnFor(wordInOriginal);
      //      addVariant(column, witness, wordInWitness);
    }

    List<NonMatch> additions = compresult.getAdditions();
    for (NonMatch addition : additions) {

      List<Word> witnessWords = addition.getWitness().getWords();
      Gap base = addition.getBase();
      addVariantAtGap(superbase, witness, base, witnessWords);
    }
  }

  private void addVariantAtGap(Superbase superbase, Witness witness, Gap baseGap, List<Word> witnessWords) {
    if (baseGap.isAtTheEnd()) {
      addVariantAtTheEnd(witness, witnessWords);
    } else {
      Word nextWord = baseGap.getNextWord();
      Column column = superbase.getColumnFor(nextWord);
      addVariantBefore(column, witness, witnessWords);
    }
  }

  public List<Column> getColumns() {
    return columns;
  }

  public void addVariant(Column column, Witness witness, Word wordInWitness) {
    column.addVariant(witness, wordInWitness);
    addWitnessToInternalList(witness);
  }

  public void addVariantBefore(Column column, Witness witness, List<Word> witnessWords) {
    int indexOf = columns.indexOf(column);
    if (indexOf == -1) {
      throw new RuntimeException("Unexpected error: Column not found!");
    }

    for (Word word : witnessWords) {
      Column extraColumn = new Column(witness, word);
      columns.add(indexOf, extraColumn);
      indexOf++;
    }
    addWitnessToInternalList(witness);
  }

  public void addVariantAtTheEnd(Witness witness, List<Word> witnessWords) {
    for (Word word : witnessWords) {
      Column extraColumn = new Column(witness, word);
      columns.add(extraColumn);
    }
    addWitnessToInternalList(witness);
  }
}
