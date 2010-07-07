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

package eu.interedition.collatex.experimental.ngrams.alignment;


import com.sd_editions.collatex.match.views.AppElement;

import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex.experimental.ngrams.table.AlignmentTable;

public class ModificationVisitor {

  private final AlignmentTable table;

  public ModificationVisitor(final AlignmentTable table) {
    this.table = table;
  }

  public void visitAddition(final Addition addition) {
    final NGram addedWords = addition.getAddedWords();
    final AppElement app = new AppElement(addedWords);
    table.setApp(addition.getPosition() * 2 - 2, app);
  }

  public void visitOmission(final Omission omission) {
    final NGram omittedWords = omission.getOmittedWords();
    final AppElement app = new AppElement(omittedWords);
    table.setApp(omission.getPosition() * 2 - 1, app);
  }

  public void visitReplacement(final Replacement replacement) {
    final NGram lemma = replacement.getOriginalWords();
    final NGram reading = replacement.getReplacementWords();
    final AppElement app = new AppElement(lemma, reading);
    table.setApp(replacement.getPosition() * 2 - 1, app);
  }

}
