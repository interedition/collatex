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

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.Subsegment;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class NewSuperbase extends WitnessSegmentPhrases {
  private final List<Column<Phrase>> _columns;

  public NewSuperbase() {
    super("superbase");
    _columns = Lists.newArrayList();
  }

  public static NewSuperbase create(final AlignmentTable2<Phrase> table) {
    final NewSuperbase newS = new NewSuperbase();
    for (final Column<Phrase> column : table.getColumns()) {
      final List<Phrase> unique = column.getUniqueElements();
      for (final Phrase phrase : unique) {
        newS.addPhrase(phrase, column);
      }
    }
    return newS;
  }

  public void addPhrase(final Phrase phrase, final Column<Phrase> column) {
    // here we make a new phrase ... to set the positions, subsegment, and witnessID?!
    // we fake the length as 1
    final int position = getPhrases().size() + 1;
    final Subsegment subsegment = phrase.getSubsegment();
    final Phrase faked = new SuperbasePhrase(position, subsegment);
    getPhrases().add(faked);
    _columns.add(column);
  }

  public Column<Phrase> getColumnFor(final Match<Phrase> match) {
    return getColumnFor(match.getBaseWord());
  }

  public Column<Phrase> getColumnFor(final Phrase phraseA) {
    final int position = phraseA.getStartPosition();
    final Column<Phrase> column = _columns.get(position - 1);
    return column;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("Superbase: (");
    String delimiter = "";
    for (final Phrase p : getPhrases()) {
      result.append(delimiter).append(p.getSubsegment().getTitle());
      delimiter = ", ";
    }
    result.append(")");
    return result.toString();
  }

}
