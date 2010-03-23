package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.ISuperbase;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableCreator3 {
  public static IAlignmentTable createAlignmentTable(final List<IWitness> set) {
    final IAlignmentTable table = new AlignmentTable4();
    for (final IWitness witness : set) {
      AlignmentTableCreator3.addWitness(table, witness);
    }
    return table;
  }

  private static void addWitness(final IAlignmentTable table, final IWitness witness) {
    if (table.getSigli().isEmpty()) {
      for (final INormalizedToken token : witness.getTokens()) {
        table.add(new Column3(token));
      }
      table.getSigli().add(witness.getSigil());
      return;
    }

    // hey! that is duplicated!
    table.getSigli().add(witness.getSigil());

    // make the superbase from the alignment table
    // TODO: figure out first the columns affected?
    final ISuperbase superbase = Superbase4.create(table);
    final Factory factory = new Factory();
    final IAlignment alignment = factory.createAlignment(superbase, witness);
    addMatchesToAlignmentTable(superbase, alignment);
    addReplacementsToAlignmentTable(superbase, alignment);
  }

  private static void addReplacementsToAlignmentTable(final ISuperbase superbase, final IAlignment alignment) {
    final List<IReplacement> replacements = alignment.getReplacements();
    for (final IReplacement replacement : replacements) {
      addReplacement(replacement, superbase);
    }
  }

  //NOTE: for now we assume that phraseA and PhraseB have the same length!
  private static void addReplacement(final IReplacement replacement, final ISuperbase superbase) {
    final List<IColumn> columns = determineColumns(replacement.getOriginalWords(), superbase);
    placePhraseInColumns(replacement.getReplacementWords(), columns);
  }

  //NOTE: for now we assume that phraseA is longer than phraseB!
  //NOTE: this method is only for variants!
  private static void placePhraseInColumns(final IPhrase phraseB, final List<IColumn> columns) {
    if (phraseB.size() > columns.size()) {
      // System.out.println(columns.size());
      // System.out.println(phraseB.size());
      throw new RuntimeException("The phrase to be placed in the table is longer than columns!");
    }
    final List<INormalizedToken> tokens = phraseB.getTokens();
    for (int i = 0; i < phraseB.size(); i++) {
      final IColumn column = columns.get(i);
      final INormalizedToken token = tokens.get(i);
      column.addVariant(token);
    }
  }

  private static List<IColumn> determineColumns(final IPhrase phraseA, final ISuperbase superbase) {
    System.out.println(phraseA);
    final List<IColumn> columns = Lists.newArrayList();
    for (final INormalizedToken tokenA : phraseA.getTokens()) {
      final IColumn column = superbase.getColumnFor(tokenA);
      columns.add(column);
    }
    return columns;
  }

  static void addMatchesToAlignmentTable(final ISuperbase superbase, final IAlignment alignment) {
    final List<IMatch> matches = alignment.getMatches();
    //    System.out.println(superbase.toString());
    //    System.out.println("!!!Matches!!!" + matches);

    // TODO: first extract a List of Columns!
    // TODO: Use the determine columns method!
    // TODO: extract handle match method!
    for (final IMatch match : matches) {
      final IPhrase phraseA = match.getPhraseA();
      final IPhrase phraseB = match.getPhraseB();
      final Iterator<INormalizedToken> iterator = phraseB.getTokens().iterator();
      for (final INormalizedToken tokenA : phraseA.getTokens()) {
        final INormalizedToken tokenB = iterator.next();
        //the next lines are the essence!
        final IColumn column = superbase.getColumnFor(tokenA);
        column.addMatch(tokenB);
      }
    }
  }
}
