package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ISuperbase;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableCreator3 {
  public static IAlignmentTable createAlignmentTable(final List<IWitness> set) {
    final IAlignmentTable table = new AlignmentTable4();
    for (final IWitness witness : set) {
      AlignmentTableCreator3.addWitness(table, witness);
    }
    //TODO: more here?
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
    final ISuperbase superbase = Superbase4.create(table);
    final Factory factory = new Factory();
    final IAlignment alignment = factory.createAlignment(superbase, witness);
    addMatchesToAlignmentTable(superbase, alignment);
  }

  static void addMatchesToAlignmentTable(final ISuperbase superbase, final IAlignment alignment) {
    final List<IMatch> matches = alignment.getMatches();
    //    System.out.println(superbase.toString());
    //    System.out.println("!!!Matches!!!" + matches);

    for (final IMatch match : matches) {
      //Note: here it would be more convenient to have matches of tokens
      // instead of matches of phrases
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
