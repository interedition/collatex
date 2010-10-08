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

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class Superbase extends Segment {
  private final List<Column> columnForEachWord;

  public Superbase() {
    this.columnForEachWord = Lists.newArrayList();
  }

  public void addWord(Word word, Column column) {
    Word newWord = new Word("sb", word.original, getWords().size() + 1);
    getWords().add(newWord);
    columnForEachWord.add(column);
  }

  public Column getColumnFor(Match<Word> match) {
    // Note: this piece of code was meant to handle transposed matches!
    // matchesOrderedForTheWitness and matchesOrderedForTheBase were parameters!
    //    int indexOfMatchInWitness = matchesOrderedForTheWitness.indexOf(match);
    //    Match transposedmatch = matchesOrderedForTheBase.get(indexOfMatchInWitness);
    //    Word baseWord = transposedmatch.getBaseWord();
    Word baseWord = match.getBaseWord();
    Column column = getColumnFor(baseWord);
    return column;
  }

  public Column getColumnFor(Word word) {
    int indexOf = getWords().indexOf(word);
    if (indexOf == -1) {
      throw new RuntimeException("Unexpected error: no column found for word: " + word);
    }
    Column column = columnForEachWord.get(indexOf);
    if (column == null) {
      throw new RuntimeException(word.toString() + " not in alignment table!");
    }
    return column;
  }

  //  public void setColumn(int position, Column column) {
  //    columnForEachWord.set(position - 1, column);
  //  }
}
