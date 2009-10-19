package eu.interedition.collatex.alignment.multiple_witness;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.functions.Matcher;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class AlignmentTableCreator {

  public static AlignmentTable2 createAlignmentTable(WitnessSet set) {
    AlignmentTable2 table = new AlignmentTable2();
    for (Witness witness : set.getWitnesses()) {
      AlignmentTableCreator.addWitness(table, witness);
    }
    return table;
  }

  static void addWitness(AlignmentTable2 table, Witness witness) {
    if (table.getWitnesses().isEmpty()) {
      for (Word word : witness.getWords()) {
        table.add(new Column(word));
      }
      table.getWitnesses().add(witness);
      return;
    }

    table.addWitnessToInternalList(witness);

    // make the superbase from the alignment table
    Superbase superbase = table.createSuperbase();
    Alignment alignment = Matcher.align(superbase, witness);

    addMatchesToAlignmentTable(superbase, alignment);
    addReplacementsToAlignmentTable(table, witness, superbase, alignment);
    addAdditionsToAlignmentTable(table, superbase, alignment);
  }

  static void addAdditionsToAlignmentTable(AlignmentTable2 table, Superbase superbase, Alignment compresult) {
    List<Gap> additions = compresult.getAdditions();
    for (Gap addition : additions) {
      List<Word> witnessWords = addition.getPhraseB().getWords();
      AlignmentTableCreator.addVariantAtGap(table, superbase, addition, witnessWords);
    }
  }

  // TODO: addReplacements.. should look like addAdditions method!
  static void addReplacementsToAlignmentTable(AlignmentTable2 table, Witness witness, Superbase superbase, Alignment compresult) {
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
            table.addVariantBefore(column, Lists.newArrayList(wordInWitness)); // FIXME but this doesn't handle longer sequences ...
          } else {
            column.addVariant(wordInWitness);
          }
        }
      }
      // still have words in the witness? add new columns after the last one from the base
      if (witnessIterator.hasNext()) {
        LinkedList<Word> remainingWitnessWords = Lists.newLinkedList(witnessIterator);
        AlignmentTableCreator.addVariantAtGap(table, superbase, replacement, remainingWitnessWords);
      }
    }
  }

  static void addMatchesToAlignmentTable(Superbase superbase, Alignment compresult) {
    Set<Match> matches = compresult.getMatches();
    for (Match match : matches) {
      Column column = superbase.getColumnFor(match);
      Word witnessWord = match.getWitnessWord();
      column.addMatch(witnessWord);
    }
  }

  static void addVariantAtGap(AlignmentTable2 table, Superbase superbase, Gap gap, List<Word> witnessWords) {
    if (gap.getPhraseA().isAtTheEnd()) {
      table.addVariantAtTheEnd(witnessWords);
    } else {
      Match nextMatch = gap.getNextMatch();
      Column column = superbase.getColumnFor(nextMatch);
      table.addVariantBefore(column, witnessWords);
    }
  }

}
