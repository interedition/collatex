package eu.interedition.collatex.superbase;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.collation.Collation;
import eu.interedition.collatex.collation.alignment.Match;
import eu.interedition.collatex.collation.alignment.Matcher;
import eu.interedition.collatex.collation.gaps.Gap;
import eu.interedition.collatex.collation.sequences.MatchSequence;
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
    // NOTE: OLD COLLATION CODE!
    //    CollateCore core = new CollateCore();
    //    MatchNonMatch compresult = core.compareWitnesses(superbase, witness);
    Matcher matcher = new Matcher();
    Collation compresult = matcher.collate(superbase, witness);
    List<MatchSequence> matchSequencesForBase = compresult.getMatchSequencesForBase();
    List<MatchSequence> matchSequencesForWitness = compresult.getMatchSequencesForWitness();
    // I just need it as a list of matches
    // Note: this list must be already present somewhere
    // you might as well take the set of matches
    // TODO: I could push this to the CompResult
    List<Match> matchesOrderedForTheWitness = Lists.newArrayList();
    for (MatchSequence matchSeq : matchSequencesForWitness) {
      for (Match match : matchSeq.getMatches()) {
        matchesOrderedForTheWitness.add(match);
      }
    }
    List<Match> matchesOrderedForTheBase = Lists.newArrayList();
    for (MatchSequence matchSeq : matchSequencesForBase) {
      for (Match match : matchSeq.getMatches()) {
        matchesOrderedForTheBase.add(match);
      }
    }
    Set<Match> matches = compresult.getMatches();
    for (Match match : matches) {
      Column column = getColumnForThisMatch(superbase, matchesOrderedForTheWitness, matchesOrderedForTheBase, match);
      Word witnessWord = match.getWitnessWord();
      addMatch(witness, witnessWord, column);
    }

    List<Gap> replacements = compresult.getReplacements();
    for (Gap replacement : replacements) {
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
        addVariantAtGap(superbase, witness, replacement, remainingWitnessWords, matchesOrderedForTheWitness, matchesOrderedForTheBase);
      }

      //      Word wordInOriginal = replacement.getBase().getFirstWord();
      //      Word wordInWitness = replacement.getWitness().getFirstWord(); // if witness is longer -> extra columns
      //      Column column = superbase.getColumnFor(wordInOriginal);
      //      addVariant(column, witness, wordInWitness);
    }

    List<Gap> additions = compresult.getAdditions();
    for (Gap addition : additions) {

      List<Word> witnessWords = addition.getWitness().getWords();
      addVariantAtGap(superbase, witness, addition, witnessWords, matchesOrderedForTheWitness, matchesOrderedForTheBase);
    }
  }

  private Column getColumnForThisMatch(Superbase superbase, List<Match> matchesOrderedForTheWitness, List<Match> matchesOrderedForTheBase, Match match) {
    int indexOfMatchInWitness = matchesOrderedForTheWitness.indexOf(match);
    Match transposedmatch = matchesOrderedForTheBase.get(indexOfMatchInWitness);
    Word baseWord = transposedmatch.getBaseWord();
    Column column = superbase.getColumnFor(baseWord);
    return column;
  }

  private void addVariantAtGap(Superbase superbase, Witness witness, Gap gap, List<Word> witnessWords, List<Match> matchesOrderedForTheWitness, List<Match> matchesOrderedForTheBase) {
    if (gap.getBase().isAtTheEnd()) {
      addVariantAtTheEnd(witness, witnessWords);
    } else {
      // I should take the next witness match here!
      // It is strange that above I take the base gap!
      Match nextMatch = gap.getNextMatch();
      Column column = getColumnForThisMatch(superbase, matchesOrderedForTheWitness, matchesOrderedForTheBase, nextMatch);
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
