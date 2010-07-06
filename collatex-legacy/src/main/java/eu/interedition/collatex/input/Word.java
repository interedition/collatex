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

package eu.interedition.collatex.input;

import eu.interedition.collatex.input.visitors.ICollationResource;
import eu.interedition.collatex.input.visitors.IResourceVisitor;
import eu.interedition.collatex.tokenization.Token;

public class Word extends BaseElement implements ICollationResource {
  private final String witnessId;
  public final String original;
  public final String _normalized;
  public final int position;

  // TODO add punctuation!!
  public Word(final String _witnessId, final String _original, final int _position) {
    if (_original.isEmpty()) throw new IllegalArgumentException("Word cannot be empty!");
    this.witnessId = _witnessId;
    this.original = _original;
    this._normalized = original.toLowerCase().replaceAll("[`~'!@#$%^&*():;,\\.]", "");
    this.position = _position;
  }

  // TODO notice the duplication here!
  // TODO store punctuation!
  public Word(final String _witnessId, final Token nextToken, final int _position) {
    this.witnessId = _witnessId;
    this.position = _position;
    this.original = nextToken.getOriginal();
    this._normalized = nextToken.getText();
  }

  public String getNormalized() {
    return _normalized;
  }

  @Override
  public String toString() {
    return original;
  }

  @Override
  public int hashCode() {
    return original.hashCode() * (10 ^ position);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Word)) return false;
    final Word other = (Word) obj;
    return other.original.equals(this.original) && (other.position == this.position);
  }

  @Override
  public String getWitnessId() {
    return witnessId;
  }

  @Override
  public void accept(final IResourceVisitor visitor) {
    visitor.visitWord(this);
  }

  @Override
  public int getBeginPosition() {
    return position;
  }

  @Override
  public int getEndPosition() {
    return position;
  }

  @Override
  public String getOriginal() {
    return original;
  }

  @Override
  public int length() {
    return 1;
  }

}
