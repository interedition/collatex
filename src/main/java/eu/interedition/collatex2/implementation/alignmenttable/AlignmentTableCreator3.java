package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignment.Alignment;
import eu.interedition.collatex2.implementation.alignment.Gap;
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
    final IAlignment alignment = factory.createAlignmentUsingSuperbase(table, witness);
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
      table.addReplacement(replacement);
    }
  }

  private static void addMatchToAlignmentTable(final IMatch match) {
    final IColumns columns = match.getColumnsA();
    columns.addMatchPhrase(match.getPhraseB());
  }

  static void addAdditionsToAlignmentTable(final IAlignmentTable table, final IAlignment alignment) {
    final List<IAddition> additions = alignment.getAdditions();
    for (final IAddition addition : additions) {
      table.addAddition(addition);
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
    final List<IMatch> matches = removeMatchesFromTranpositions(alignment, transpositions);
    final Stack<ITransposition> transToCheck = new Stack<ITransposition>();
    final List<IGap> gaps = alignment.getGaps();
    transToCheck.addAll(transpositions);
    Collections.reverse(transToCheck);
    while (!transToCheck.isEmpty()) {
      final ITransposition top = transToCheck.pop();
      final ITransposition mirrored = findMirroredTransposition(transToCheck, top);
      //Note: this only calculates the distance between the columns.
      //Note: it does not take into account a possible distance in the prases!
      if (mirrored != null && distanceBetweenTranspositions(top, mirrored) == 0) {
        // System.out.println("Keeping: transposition " + top.toString());
        // System.out.println("Removing: transposition " + mirrored.toString());
        // remove mirrored transpositions (a->b, b->a) from transpositions
        transToCheck.remove(mirrored);
        final IGap addition = makeAdditionOutOfTransposition(top);
        gaps.add(addition);
        matches.add(top.getMatchA());
      } else {
        final IGap replacement = makeReplacementOutOfTransposition(top);
        gaps.add(replacement);
      }
    }

    final IAlignment al = new Alignment(matches, gaps);
    return al;
  }

  private static IGap makeReplacementOutOfTransposition(final ITransposition top) {
    final IColumns columns = top.getMatchA().getColumnsA();
    final IPhrase phrase = top.getMatchB().getPhraseB();
    final IColumn nextColumn = null; // TODO: this is wrong... very wrong!
    final IGap gap = new Gap(columns, phrase, nextColumn);
    return gap;
  }

  private static int distanceBetweenTranspositions(final ITransposition top, final ITransposition mirrored) {
    final int beginPosition = mirrored.getMatchA().getColumnsA().getBeginPosition();
    final int endPosition = top.getMatchA().getColumnsA().getEndPosition();
    //System.out.println(beginPosition + ":" + endPosition);
    final int distance = beginPosition - (endPosition + 1);
    // System.out.println(distance);
    return distance;
  }

  private static List<IMatch> removeMatchesFromTranpositions(final IAlignment alignment, final List<ITransposition> ntranspositions) {
    // remove matches from transpositions
    final List<IMatch> matches = alignment.getMatches();
    for (final ITransposition t : ntranspositions) {
      final IMatch witness = t.getMatchB();
      //Note: this is not nice; this removes from the original list!
      matches.remove(witness);
    }
    return matches;
  }

  private static IGap makeAdditionOutOfTransposition(final ITransposition t) {
    // make an addition from the match
    final IMatch witness = t.getMatchB();
    final IColumns columns = new Columns();
    final IPhrase phrase = witness.getPhraseB();
    // NOTE: I need the next column here!
    // NOTE: so the next column should be in the transposition!
    // TODO add next column to addition, transposition, etc
    // TODO and use that!
    final IColumn nextColumn = t.getMatchA().getColumnsA().getFirstColumn();
    final IGap addition = new Gap(columns, phrase, nextColumn);
    //Note: this is not nice; this adds to the original list!
    return addition;
  }

  private static ITransposition findMirroredTransposition(final Stack<ITransposition> transToCheck, final ITransposition original) {
    for (final ITransposition transposition : transToCheck) {
      if (transposition.getMatchA().getNormalized().equals(original.getMatchB().getNormalized())) {
        if (transposition.getMatchB().getNormalized().equals(original.getMatchA().getNormalized())) {
          return transposition;
        }
      }
    }
    return null;
  }

}
