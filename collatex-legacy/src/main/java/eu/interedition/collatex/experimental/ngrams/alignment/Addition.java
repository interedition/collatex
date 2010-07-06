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

public class Addition extends Modification {
  private final NGram addition;
  private final NGram nextMatchA;

  public Addition(final NGram nextMatchA, final NGram addition) {
    this.nextMatchA = nextMatchA;
    this.addition = addition;
  }

  public int getPosition() {
    if (nextMatchA == null || nextMatchA.isEmpty()) {
      throw new RuntimeException("There is no next match!");
    }
    return nextMatchA.getFirstToken().getPosition();
  }

  public NGram getAddedWords() {
    return addition;
  }

  @Override
  public String toString() {
    // TODO should not be get Normalized?
    String result = "addition: " + addition.getNormalized();
    // TODO I would like to have only 
    if (nextMatchA == null || nextMatchA.isEmpty()) {
      result += " position: at the end";
    } else {
      result += " position: " + getPosition();
    }
    return result;
  }

  @Override
  public void accept(final ModificationVisitor modificationVisitor) {
    modificationVisitor.visitAddition(this);
  }

}
