/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.alignment.multiple_witness;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.functions.Aligner;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class AlignmentTableCreator {

  public static AlignmentTable2 createAlignmentTable(final WitnessSet set) {
    final AlignmentTable2 table = new AlignmentTable2();
    for (final Witness witness : set.getWitnesses()) {
      AlignmentTableCreator.addWitness(table, witness.getFirstSegment());
    }
    return table;
  }

  // TODO rename to addSegment!
  static void addWitness(final AlignmentTable2 table, final Segment witness) {
    if (table.getWitnesses().isEmpty()) {
      for (final Word word : witness.getWords()) {
        table.add(new Column(word));
      }
      table.getWitnesses().add(witness);
      table.getSigli().add(witness.id);
      return;
    }

    table.addWitnessToInternalList(witness);

    // TODO remove call to Aligner!
    // make the superbase from the alignment table
    final Superbase superbase = table.createSuperbase();
    final Alignment alignment = Aligner.align(superbase, witness);
    final Alignment better = NewAlignmentTableCreator.makeAddDelFromTrans(null, witness, alignment);
    addMatchesToAlignmentTable(superbase, better);
    addReplacementsToAlignmentTable(table, witness, superbase, better);
    addAdditionsToAlignmentTable(table, superbase, better);
  }

  static void addAdditionsToAlignmentTable(final AlignmentTable2 table, final Superbase superbase, final Alignment compresult) {
    final List<Gap> additions = compresult.getAdditions();
    for (final Gap addition : additions) {
      final List<Word> witnessWords = addition.getPhraseB().getWords();
      AlignmentTableCreator.addVariantAtGap(table, superbase, addition, witnessWords);
    }
  }

  // TODO addReplacements.. should look like addAdditions method!
  static void addReplacementsToAlignmentTable(final AlignmentTable2 table, final Segment witness, final Superbase superbase, final Alignment compresult) {
    final List<Gap> replacements = compresult.getReplacements();
    for (final Gap replacement : replacements) {
      // TODO hou rekening met langere additions!

      final Iterator<Word> baseIterator = replacement.getPhraseA().getWords().iterator();
      final Iterator<Word> witnessIterator = replacement.getPhraseB().getWords().iterator();
      while (baseIterator.hasNext()) {
        final Word wordInOriginal = baseIterator.next();
        final Column column = superbase.getColumnFor(wordInOriginal);
        if (witnessIterator.hasNext()) {
          final Word wordInWitness = witnessIterator.next();
          if (column.containsWitness(witness)) { // already have something in here from the matches phase
            table.addVariantBefore(column, Lists.newArrayList(wordInWitness)); // FIXME but this doesn't handle longer sequences ...
          } else {
            column.addVariant(wordInWitness);
          }
        }
      }
      // still have words in the witness? add new columns after the last one from the base
      if (witnessIterator.hasNext()) {
        final List<Word> remainingWitnessWords = Lists.newArrayList(witnessIterator);
        AlignmentTableCreator.addVariantAtGap(table, superbase, replacement, remainingWitnessWords);
      }
    }
  }

  static void addMatchesToAlignmentTable(final Superbase superbase, final Alignment compresult) {
    final Set<Match> matches = compresult.getMatches();
    for (final Match<Word> match : matches) {
      final Column column = superbase.getColumnFor(match);
      final Word witnessWord = match.getWitnessWord();
      column.addMatch(witnessWord);
    }
  }

  static void addVariantAtGap(final AlignmentTable2 table, final Superbase superbase, final Gap gap, final List<Word> witnessWords) {
    if (gap.isAtTheEnd()) {
      table.addVariantAtTheEnd(witnessWords);
    } else {
      final Match nextMatch = gap.getNextMatch();
      final Column column = superbase.getColumnFor(nextMatch);
      table.addVariantBefore(column, witnessWords);
    }
  }

}
