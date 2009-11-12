package eu.interedition.collatex.alignment.multiple_witness;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.LeftToRightMatcher;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.BaseContainer;
import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.input.BaseElement;
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

    final Alignment<Phrase> better = NewAlignmentTableCreator.makeAddDelFromTrans(null, seg, alignment);
    addMatchesToAlignmentTable(superbase, better);
    addReplacementsToAlignmentTable(table, seg, superbase, better);
    addAdditionsToAlignmentTable(table, superbase, better);
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
      //      System.out.println(replacement.toString());
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
    if (gap.isAtTheEnd()) {
      table.addVariantAtTheEnd(witnessWords);
    } else {
      // throw new RuntimeException("NOT IMPLEMENTED YET!");
      final Match<Phrase> nextMatch = gap.getNextMatch();
      final Column<Phrase> column = superbase.getColumnFor(nextMatch);
      table.addVariantBefore(column, witnessWords);
    }
  }

  // TODO: make this method generic or work with Phrase,
  // TODO; remove dependency on Word
  // NOTE: the way transpositions are handled here
  // and the way there are placed in the alignment table
  // is just one specific case, namely
  // a b
  // b a
  // this becomes |a| b| |, | |b|a|
  public static <T extends BaseElement> Alignment<T> makeAddDelFromTrans(final BaseContainer<T> a, final BaseContainer<T> b, final Alignment<T> alignment) {
    // handle transpositions here!
    final Collection<Transposition> tranpositions = alignment.getTranpositions();
    System.out.println(tranpositions);
    // TODO: DO CHECK HERE!!!!
    // remove duplicates from transpositions
    final Stack<Transposition> transToCheck = new Stack<Transposition>();
    final List<Transposition> transpositions = Lists.newArrayList();
    transToCheck.addAll(tranpositions);
    while (!transToCheck.isEmpty()) {
      final Transposition top = transToCheck.pop();
      transpositions.add(top);
      for (final Transposition tr : transToCheck) {
        if (tr.getBase().equals(top.getWitness())) {
          if (tr.getWitness().equals(top.getBase())) {
            transToCheck.remove(tr);
            break;
          }
        }
      }
    }
    // remove matches from transpositions
    final Set<Match<T>> matches = alignment.getMatches();
    final List<Gap> gaps = alignment.getGaps();
    //    System.out.println(transpositions);
    for (final Transposition t : transpositions) {
      final MatchSequence<T> witness = t.getWitness();
      for (final Match<T> match : witness.getMatches()) {
        //        System.out.println("WHAT? " + match);
        matches.remove(match);
      }
      // make an addition from the matchSequence
      // NOTE: I need the next match here!
      // NOTE: so the next match should be in the TranspositioN!
      final T w = (T) t.getWitness().getFirstMatch().getWitnessWord();
      final T o = (T) t.getWitness().getLastMatch().getWitnessWord();
      //      System.out.println(w);
      //      System.out.println(o);
      final Match<T> nextMatch = t.getBase().getFirstMatch();
      // TODO: remove nextWord from BaseContainerPart
      // TODO: add next match to addition, transposition, etc
      // TODO: and use that!
      // TODO: rename word to element!
      final T nextBaseWord;
      // TODO: make a method on Transposition called hasNextMatch!
      if (nextMatch != null) {
        nextBaseWord = nextMatch.getBaseWord();
      } else {
        nextBaseWord = null;
      }
      final BaseContainerPart<T> partNull = new BaseContainerPart<T>(null, 0, 0, 0);
      final BaseContainerPart<T> partAdd = new BaseContainerPart<T>(b, w, o);
      final Gap addition = new Gap(partNull, partAdd, nextMatch);
      //      System.out.println(partAdd.hasGap());
      //      System.out.println(addition.isAddition());
      gaps.add(addition);
      System.out.println("Hey heb addition geadd: " + addition.toString());

    }
    final Alignment<T> al = Alignment.create2(matches, gaps, alignment.getMatchSequencesOrderedForWitnessA(), alignment.getMatchSequencesOrderedForWitnessB());
    return al;
  }

  //  // I just need it as a list of matches
  //  List<MatchSequence> matchSequencesForBase = compresult.getMatchSequencesOrderedForWitnessA();
  //  List<MatchSequence> matchSequencesForWitness = compresult.getMatchSequencesOrderedForWitnessB();
  //  List<Match> matchesOrderedForTheWitness = Lists.newArrayList();
  //  for (MatchSequence matchSeq : matchSequencesForWitness) {
  //    for (Match match : matchSeq.getMatches()) {
  //      matchesOrderedForTheWitness.add(match);
  //    }
  //  }
  //  List<Match> matchesOrderedForTheBase = Lists.newArrayList();
  //  for (MatchSequence matchSeq : matchSequencesForBase) {
  //    for (Match match : matchSeq.getMatches()) {
  //      matchesOrderedForTheBase.add(match);
  //    }
  //  }

}
