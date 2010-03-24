package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.List;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.ISuperbase;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableCreator3 {
  public static IAlignmentTable createAlignmentTable(final List<IWitness> set, final ICallback callback) {
    final IAlignmentTable table = new AlignmentTable4();
    for (final IWitness witness : set) {
      AlignmentTableCreator3.addWitness(table, witness, callback);
    }
    return table;
  }

  private static void addWitness(final IAlignmentTable table, final IWitness witness, final ICallback callback) {
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
    callback.alignment(alignment);
    addMatchesToAlignmentTable(superbase, alignment);
    addReplacementsToAlignmentTable(superbase, alignment);
  }

  static void addMatchesToAlignmentTable(final ISuperbase superbase, final IAlignment alignment) {
    final List<IMatch> matches = alignment.getMatches();
    for (final IMatch match : matches) {
      addMatchToAlignmentTable(superbase, match);
    }
  }

  private static void addReplacementsToAlignmentTable(final ISuperbase superbase, final IAlignment alignment) {
    final List<IReplacement> replacements = alignment.getReplacements();
    for (final IReplacement replacement : replacements) {
      addReplacement(replacement, superbase);
    }
  }

  private static void addMatchToAlignmentTable(final ISuperbase superbase, final IMatch match) {
    final IColumns columns = superbase.getColumnsFor(match.getPhraseA());
    columns.addMatchPhrase(match.getPhraseB());
  }

  //NOTE: for now we assume that phraseA and PhraseB have the same length!
  private static void addReplacement(final IReplacement replacement, final ISuperbase superbase) {
    final IColumns columns = superbase.getColumnsFor(replacement.getOriginalWords());
    columns.addVariantPhrase(replacement.getReplacementWords());
  }

}
