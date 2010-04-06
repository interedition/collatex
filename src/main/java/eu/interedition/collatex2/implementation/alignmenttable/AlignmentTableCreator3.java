package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignment.Alignment;
import eu.interedition.collatex2.implementation.alignment.Gap;
import eu.interedition.collatex2.implementation.modifications.Addition;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.ITransposition;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableCreator3 {
  public static IAlignmentTable createAlignmentTable(final List<IWitness> witnessList, final ICallback callback) {
    Factory.createWitnessIndexMap(witnessList);
    final IAlignmentTable table = new AlignmentTable4();
    for (final IWitness witness : witnessList) {
      AlignmentTableCreator3.addWitness(table, witness, callback);
    }
    return table;
  }

  public static void addWitness(final IAlignmentTable table, final IWitness witness, final ICallback callback) {
    final boolean tableIsEmpty = table.getSigli().isEmpty();
    table.getSigli().add(witness.getSigil());
    if (tableIsEmpty) {
      for (final INormalizedToken token : witness.getTokens()) {
        table.add(new Column3(token, table.size() + 1));
      }
      return;
    }

    final Factory factory = new Factory();
    final IAlignment alignment = factory.createAlignment(table, witness);
    callback.alignment(alignment);
    final IAlignment alignment2 = makeAddDelFromTrans(alignment);
    addMatchesToAlignmentTable(alignment2);
    addReplacementsToAlignmentTable(table, alignment2);
    addAdditionsToAlignmentTable(table, alignment2);
  }

  static void addMatchesToAlignmentTable(final IAlignment alignment) {
    final List<IMatch> matches = alignment.getMatches();
    for (final IMatch match : matches) {
      addMatchToAlignmentTable(match);
    }
  }

  private static void addReplacementsToAlignmentTable(final IAlignmentTable table, final IAlignment alignment) {
    final List<IReplacement> replacements = alignment.getReplacements();
    for (final IReplacement replacement : replacements) {
      addReplacement(table, replacement);
    }
  }

  private static void addMatchToAlignmentTable(final IMatch match) {
    final IColumns columns = match.getColumnsA();
    columns.addMatchPhrase(match.getPhraseB());
  }

  private static void addReplacement(final IAlignmentTable table, final IReplacement replacement) {
    final IColumns columns = replacement.getOriginalColumns();
    final IPhrase replacementPhrase = replacement.getReplacementPhrase();
    if (replacementPhrase.size() > columns.size()) {
      final IColumn nextColumn = replacement.getNextColumn();
      final IPhrase additionalPhrase = replacementPhrase.createSubPhrase(columns.size() + 1, replacementPhrase.size());
      //System.out.println("this should be additional: " + additionalPhrase);
      final IPhrase smallerPhrase = replacementPhrase.createSubPhrase(1, columns.size());
      //System.out.println("this should be the replacement: " + smallerPhrase);
      final IAddition addition = new Addition(nextColumn, additionalPhrase);
      columns.addVariantPhrase(smallerPhrase);
      addVariantAtGap(table, addition, additionalPhrase);
    } else {
      columns.addVariantPhrase(replacementPhrase);
    }
  }

  static void addAdditionsToAlignmentTable(final IAlignmentTable table, final IAlignment alignment) {
    final List<IAddition> additions = alignment.getAdditions();
    for (final IAddition addition : additions) {
      final IPhrase witnessPhrase = addition.getAddedPhrase();
      AlignmentTableCreator3.addVariantAtGap(table, addition, witnessPhrase);
    }
  }

  //TODO: the number of parameters here is strange!
  private static void addVariantAtGap(final IAlignmentTable table, final IAddition addition, final IPhrase witnessPhrase) {
    if (addition.isAtTheEnd()) {
      table.addVariantAtTheEnd(witnessPhrase);
    } else {
      final IColumn column = addition.getNextColumn();
      table.addVariantBefore(column, witnessPhrase);
    }
  }

  // NOTE: the way transpositions are handled here
  // and the way there are placed in the alignment table
  // is just one specific case, namely
  // a b
  // b a
  // this becomes |a| b| |, | |b|a|
  public static IAlignment makeAddDelFromTrans(final IAlignment alignment) {
    // handle transpositions here!
    final List<ITransposition> transpositions = alignment.getTranspositions();
    // System.out.println(transpositions);
    // TODO DO CHECK HERE!!!!
    // TODO: extract method!
    // remove mirrored transpositions (a->b, b->a) from transpositions
    final Stack<ITransposition> transToCheck = new Stack<ITransposition>();
    final List<ITransposition> ntranspositions = Lists.newArrayList();
    transToCheck.addAll(transpositions);
    Collections.reverse(transToCheck);
    while (!transToCheck.isEmpty()) {
      final ITransposition top = transToCheck.pop();
      // System.out.println("Keeping: transposition " + top.toString());
      ntranspositions.add(top);
      for (final ITransposition tr : transToCheck) {
        if (tr.getMatchA().getNormalized().equals(top.getMatchB().getNormalized())) {
          if (tr.getMatchB().getNormalized().equals(top.getMatchA().getNormalized())) {
            // System.out.println("Removing: transposition " + tr.toString());
            transToCheck.remove(tr);
            break;
          }
        }
      }
    }
    // remove matches from transpositions
    final List<IMatch> matches = alignment.getMatches();
    final List<IGap> gaps = alignment.getGaps();
    //    System.out.println(transpositions);
    for (final ITransposition t : ntranspositions) {
      final IMatch witness = t.getMatchB();
      //Note: this is not nice; this removes from the original list!
      matches.remove(witness);
      // make an addition from the match
      final IColumns columns = new Columns();
      final IPhrase phrase = witness.getPhraseB();
      // NOTE: I need the next column here!
      // NOTE: so the next column should be in the transposition!
      // TODO add next column to addition, transposition, etc
      // TODO and use that!
      final IColumn nextColumn = t.getMatchA().getColumnsA().getFirstColumn();
      final IGap addition = new Gap(columns, phrase, nextColumn);
      //Note: this is not nice; this adds to the original list!
      gaps.add(addition);
      //System.out.println("Hey heb addition geadd: " + addition.toString());
    }
    final IAlignment al = new Alignment(matches, gaps);
    return al;
  }
}
