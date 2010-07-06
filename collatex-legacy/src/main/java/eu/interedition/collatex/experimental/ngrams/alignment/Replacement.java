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


import eu.interedition.collatex.experimental.ngrams.NGram;

public class Replacement extends Modification {
  private final NGram _original;
  private final NGram _replacement;

  public Replacement(final NGram original, final NGram replacement) {
    this._original = original;
    this._replacement = replacement;
  }

  @Override
  public String toString() {
    // TODO Not getNormalized!
    final String baseWords = _original.getNormalized();
    // TODO Not getNormalized!
    final String replacementWords = _replacement.getNormalized();
    return "replacement: " + baseWords + " / " + replacementWords + " position: " + _original.getFirstToken().getPosition();
  }

  public int getPosition() {
    return _original.getFirstToken().getPosition();
  }

  public NGram getOriginalWords() {
    return _original;
  }

  public NGram getReplacementWords() {
    return _replacement;
  }

  @Override
  public void accept(final ModificationVisitor modificationVisitor) {
    modificationVisitor.visitReplacement(this);
  }

}
