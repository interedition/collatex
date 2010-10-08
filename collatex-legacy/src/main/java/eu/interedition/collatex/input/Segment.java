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

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Util;
import eu.interedition.collatex.input.visitors.ICollationResource;
import eu.interedition.collatex.input.visitors.IResourceVisitor;

@Deprecated
public class Segment extends BaseContainer<Word> implements ICollationResource {
  public final String id;
  private final List<Word> words;

  public Segment(final Word... _words) {
    if (_words == null) {
      throw new IllegalArgumentException("List of words cannot be null.");
    }
    if (_words.length == 0) {
      this.id = Util.generateRandomId();
    } else {
      this.id = _words[0].getWitnessId();
    }
    this.words = Lists.newArrayList(_words);
  }

  public Segment(final String _id, final List<Word> _words) {
    this.id = _id;
    this.words = _words;
  }

  public List<Word> getWords() {
    return words;
  }

  @Override
  public Word getElementOnWordPosition(final int position) {
    return words.get(position - 1);
  }

  @Override
  public int wordSize() {
    return words.size();
  }

  // Note: part copied from Phrase
  @Override
  public String toString() {
    StringBuilder replacementString = new StringBuilder();
    String divider = "";
    for (final Word word : words) {
      replacementString.append(divider).append(word);
      divider = " ";
    }
    return replacementString.toString();
  }

  public void accept(final IResourceVisitor visitor) {
    visitor.visitSegment(this);
    final List<Word> words2 = getWords();
    for (final Word word : words2) {
      visitor.visitWord(word);
    }
    visitor.postVisitWitness(this);
  }

  public String getWitnessId() {
    // TODO use the actual witnessId, this is the segmentId
    // Note: the note above is no longer correct!
    return id;
  }
}
