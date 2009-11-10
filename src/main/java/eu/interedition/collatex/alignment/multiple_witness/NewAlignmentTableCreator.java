package eu.interedition.collatex.alignment.multiple_witness;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.LeftToRightMatcher;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class NewAlignmentTableCreator {

  public static AlignmentTable2 createNewAlignmentTable(final WitnessSegmentPhrases... set) {
    final AlignmentTable2 table = new AlignmentTable2();
    for (final WitnessSegmentPhrases seg : set) {
      NewAlignmentTableCreator.addSegmentPhrases(table, seg);
    }
    return table;
  }

  private static void addSegmentPhrases(final AlignmentTable2 table, final WitnessSegmentPhrases seg) {
    if (table.getSigli().isEmpty()) {
      for (final Phrase phrase : seg.getPhrases()) {
        table.add(new Column<Phrase>(phrase));
      }
      table.getSigli().add(seg.getWitnessId());
      return;
    }

    // hey! that is duplicated!
    table.getSigli().add(seg.getWitnessId());

    // make the superbase from the alignment table
    final NewSuperbase superbase = NewSuperbase.create(table);
    final Set<Match<Phrase>> matches = LeftToRightMatcher.match(superbase, seg);
    final Alignment<Phrase> alignment = Alignment.createPhraseAlignment(matches, superbase, seg);
    //    final Alignment better = alignment.makeAddDelFromTrans(null, witness);
    addMatchesToAlignmentTable(superbase, alignment);
    addReplacementsToAlignmentTable(table, seg, superbase, alignment);
    addAdditionsToAlignmentTable(table, superbase, alignment);
  }

  static void addMatchesToAlignmentTable(final NewSuperbase superbase, final Alignment<Phrase> alignment) {
    final Set<Match<Phrase>> matches = alignment.getMatches();
    //    System.out.println(superbase.toString());
    //    System.out.println("!!!Matches!!!" + matches);

    for (final Match<Phrase> match : matches) {
      final Column<Phrase> column = superbase.getColumnFor(match);
      final Phrase witnessPhrase = match.getWitnessWord();
      column.addMatch(witnessPhrase);
    }
  }

  // TODO: make Gap generic!
  static void addAdditionsToAlignmentTable(final AlignmentTable2 table, final NewSuperbase superbase, final Alignment<Phrase> alignment) {
    final List<Gap> additions = alignment.getAdditions();
    for (final Gap addition : additions) {
      final List<Phrase> witnessWords = addition.getPhraseB().getWords();
      NewAlignmentTableCreator.addVariantAtGap(table, superbase, addition, witnessWords);
    }
  }

  // TODO: make Gap generic!
  // NOTE: addReplacements.. should look like addAdditions method?
  static void addReplacementsToAlignmentTable(final AlignmentTable2 table, final WitnessSegmentPhrases witness, final NewSuperbase superbase, final Alignment<Phrase> alignment) {
    final List<Gap> replacements = alignment.getReplacements();
    for (final Gap replacement : replacements) {
      System.out.println(replacement.toString());
      final BaseContainerPart<Phrase> partA = replacement.getPhraseA();
      final BaseContainerPart<Phrase> partB = replacement.getPhraseB();
      final List<Phrase> phrasesA = partA.getWords();
      final List<Phrase> phrasesB = partB.getWords();
      final Iterator<Phrase> iteratorA = phrasesA.iterator();
      final Iterator<Phrase> iteratorB = phrasesB.iterator();
      while (iteratorA.hasNext()) {
        final Phrase phraseA = iteratorA.next();
        // TODO: dangerous! replacement might be shorter!
        // this can happen when there is variation in a column,
        // then one phrase can replace multiple phrases!
        // maybe detect the number of columns in the phrases
        // first? then make a decision?
        if (iteratorB.hasNext()) {
          final Phrase phraseB = iteratorB.next();
          final Column<Phrase> column = superbase.getColumnFor(phraseA);
          column.addVariant(phraseB);
        }
      }
      // TODO: you might miss stuff here! replacement might be longer!
      // still have words in the witness? add new columns after the last one from the base
      if (iteratorB.hasNext()) {
        final LinkedList<Phrase> remainingWitnessWords = Lists.newLinkedList(iteratorB);
        NewAlignmentTableCreator.addVariantAtGap(table, superbase, replacement, remainingWitnessWords);
      }
    }

    //    final List<Gap> replacements = compresult.getReplacements();
    //    for (final Gap replacement : replacements) {
    //      // TODO: hou rekening met langere additions!
    //
    //      final Iterator<Word> baseIterator = replacement.getPhraseA().getWords().iterator();
    //      final Iterator<Word> witnessIterator = replacement.getPhraseB().getWords().iterator();
    //      while (baseIterator.hasNext()) {
    //        final Word wordInOriginal = baseIterator.next();
    //        final Column column = superbase.getColumnFor(wordInOriginal);
    //        if (witnessIterator.hasNext()) {
    //          final Word wordInWitness = witnessIterator.next();
    //          if (column.containsWitness(witness)) { // already have something in here from the matches phase
    //            table.addVariantBefore(column, Lists.newArrayList(wordInWitness)); // FIXME but this doesn't handle longer sequences ...
    //          } else {
    //            column.addVariant(wordInWitness);
    //          }
    //        }
    //      }
    //    }
  }

  static void addVariantAtGap(final AlignmentTable2 table, final NewSuperbase superbase, final Gap gap, final List<Phrase> witnessWords) {
    if (gap.getPhraseA().isAtTheEnd()) {
      table.addVariantAtTheEnd(witnessWords);
    } else {
      throw new RuntimeException("NOT IMPLEMENTED YET!");
      //      final Match<Phrase> nextMatch = gap.getNextMatch();
      //      final Column<Phrase> column = superbase.getColumnFor(nextMatch);
      //      table.addVariantBefore(column, witnessWords);
    }
  }

}
