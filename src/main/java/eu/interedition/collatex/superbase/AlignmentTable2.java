package eu.interedition.collatex.superbase;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.collation.Collation;
import eu.interedition.collatex.collation.alignment.Match;
import eu.interedition.collatex.collation.gaps.Gap;
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

  public void addWitness(Witness witness) {
    addWitnessToInternalList(witness);

    // make the superbase from the alignment table
    Superbase superbase = createSuperbase();
    Collation compresult = CollateCore.collate(superbase, witness);

    addMatchesToSuperbase(witness, superbase, compresult);
    addReplacementsToSuperbase(witness, superbase, compresult);
    addAdditionsToSuperbase(witness, superbase, compresult);
  }

  // Note: this is a strange method from a user point of view..
  // whether a witness is the first or not should be an implementation detail
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
  }

  private void addWitnessToInternalList(Witness witness) {
    // TODO: an ordered set instead of list would be nice here
    if (!witnesses.contains(witness)) {
      witnesses.add(witness);
    }
  }

  private void addAdditionsToSuperbase(Witness witness, Superbase superbase, Collation compresult) {
    List<Gap> additions = compresult.getAdditions();
    for (Gap addition : additions) {
      List<Word> witnessWords = addition.getPhraseB().getWords();
      addVariantAtGap(superbase, witness, addition, witnessWords);
    }
  }

  private void addReplacementsToSuperbase(Witness witness, Superbase superbase, Collation compresult) {
    List<Gap> replacements = compresult.getReplacements();
    for (Gap replacement : replacements) {
      // TODO: hou rekening met langere additions!

      Iterator<Word> baseIterator = replacement.getPhraseA().getWords().iterator();
      Iterator<Word> witnessIterator = replacement.getPhraseB().getWords().iterator();
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
        addVariantAtGap(superbase, witness, replacement, remainingWitnessWords);
      }
    }
  }

  private void addMatchesToSuperbase(Witness witness, Superbase superbase, Collation compresult) {
    Set<Match> matches = compresult.getMatches();
    for (Match match : matches) {
      Column column = getColumnForThisMatch(superbase, match);
      Word witnessWord = match.getWitnessWord();
      addMatch(witness, witnessWord, column);
    }
  }

  // Note: I could move this method to the superbase class!
  private Column getColumnForThisMatch(Superbase superbase, Match match) {
    // Note: this piece of code was meant to handle transposed matches!
    // matchesOrderedForTheWitness and matchesOrderedForTheBase were parameters!
    //    int indexOfMatchInWitness = matchesOrderedForTheWitness.indexOf(match);
    //    Match transposedmatch = matchesOrderedForTheBase.get(indexOfMatchInWitness);
    //    Word baseWord = transposedmatch.getBaseWord();
    Word baseWord = match.getBaseWord();
    Column column = superbase.getColumnFor(baseWord);
    return column;
  }

  private void addVariantAtGap(Superbase superbase, Witness witness, Gap gap, List<Word> witnessWords) {
    if (gap.getPhraseA().isAtTheEnd()) {
      addVariantAtTheEnd(witness, witnessWords);
    } else {
      // I should take the next witness match here!
      // It is strange that above I take the base gap!
      Match nextMatch = gap.getNextMatch();
      Column column = getColumnForThisMatch(superbase, nextMatch);
      addVariantBefore(column, witness, witnessWords);
    }
  }

  public List<Column> getColumns() {
    return columns;
  }

  public void addVariant(Column column, Witness witness, Word wordInWitness) {
    column.addVariant(witness, wordInWitness);
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
  }

  public void addVariantAtTheEnd(Witness witness, List<Word> witnessWords) {
    for (Word word : witnessWords) {
      Column extraColumn = new Column(witness, word);
      columns.add(extraColumn);
    }
  }
}
