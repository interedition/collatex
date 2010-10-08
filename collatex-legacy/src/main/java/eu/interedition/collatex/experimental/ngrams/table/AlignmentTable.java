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

package eu.interedition.collatex.experimental.ngrams.table;

import java.util.List;

import com.sd_editions.collatex.match.views.AppElement;
import com.sd_editions.collatex.match.views.Element;
import com.sd_editions.collatex.match.views.TextElement;

import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex.experimental.ngrams.alignment.Alignment;
import eu.interedition.collatex.experimental.ngrams.alignment.ModificationVisitor;
import eu.interedition.collatex.visualization.Modifications;

public class AlignmentTable {

  private final Element[] cells;

  // NOTE: rename to tree? XML is a tree after all!
  // NOTE: rename to elements? A tree has elements after all!
  public AlignmentTable(final Modifications modifications) {
    cells = new Element[100]; // TODO take longest witness?

    // NOTE: move this to Modifications?
    //    final Set<Match<Word>> matches = modifications.getMatches();
    //    cells = new Element[100]; // TODO take longest witness?
    //    // NOTE: move this to ModificationVisitor?
    //    for (final Match<Word> match : matches) {
    //      final Word matchedWord = match.getBaseWord();
    //      cells[matchedWord.position * 2 - 1] = new TextElement(matchedWord);
    //    }
    //    // Note: move this to Modifications?
    //    final ModificationVisitor modificationVisitor = new ModificationVisitor(this);
    //    modifications.accept(modificationVisitor);
  }

  public AlignmentTable(final Alignment alignment) {
    final List<NGram> matches = alignment.getMatches();
    cells = new Element[100]; // TODO take longest witness?
    // Note: move this to ModificationVisitor?
    for (final NGram match : matches) {
      cells[match.getFirstToken().getPosition() * 2 - 1] = new TextElement(match);
    }
    // Note: move this to Modifications?
    final ModificationVisitor modificationVisitor = new ModificationVisitor(this);
    alignment.accept(modificationVisitor);
  }

  // TODO should we store the whitespace in a Word?
  public String toXML() {
    StringBuilder result = new StringBuilder("<xml>");
    String whitespace = "";
    for (final Element element : cells) {
      if (element != null) {
        result.append(whitespace).append(element.toXML());
        whitespace = " ";
      }
    }
    result.append("</xml>");
    return result.toString();
  }

  public void setApp(final int i, final AppElement app) {
    cells[i] = app;
  }
}
