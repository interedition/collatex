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

package eu.interedition.collatex2.web;

import java.util.List;

import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IOmission;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.ITransposition;

public class HumanReadableAlignmentCallback implements ICallback {
  private static final String   BR = "<br/>";
  private final StringBuffer    buffer;
  private final IAlignmentTable table;

  public HumanReadableAlignmentCallback(IAlignmentTable table) {
    this.table = table;
    this.buffer = new StringBuffer();
  }

  @Override
  public void alignment(final IAlignment alignment) {
    displayMatches(alignment);
    displayAdditions(alignment, table);
    displayOmissions(alignment);
    displayReplacements(alignment);
    displayTranspositions(alignment);
  }

  private void displayOmissions(IAlignment alignment) {
    List<IOmission> omissions = alignment.getOmissions();
    if (!omissions.isEmpty()) {
      buffer.append("Omissions: ").append(BR);
      for (IOmission omission : omissions) {
        buffer.append(omissionView(omission));
      }
      buffer.append(BR + BR);
    }
  }

  private void displayAdditions(IAlignment alignment, IAlignmentTable table) {
    final List<IAddition> additions = alignment.getAdditions();
    if (!additions.isEmpty()) {
      buffer.append("Additions: ").append(BR);
      for (IAddition addition : additions) {
        buffer.append(additionView(addition, table));
      }
      buffer.append(BR + BR);
    }
  }

  private void displayReplacements(IAlignment alignment) {
    final List<IReplacement> replacements = alignment.getReplacements();
    if (!replacements.isEmpty()) {
      buffer.append("Replacements: ").append(BR);
      for (IReplacement replacement : replacements) {
        buffer.append(replacementView(replacement));
      }
      buffer.append(BR + BR);
    }
  }

  private String additionView(IAddition addition, IAlignmentTable table) {
    StringBuffer html = new StringBuffer("<i>" + addition.getAddedPhrase() + "</i> added ");
    // TODO: think about the case that there is no beginning nor end!
    // NOTE: extract ITable.getColumn(position)?
    if (addition.isAtTheBeginning()) {
      html.append("before <i>" + table.getColumns().get(0) + "</i>");
    } else if (addition.isAtTheEnd()) {
      html.append(" after <i>" + table.getColumns().get(table.size() - 1) + "</i>");
    } else {
      // TODO: you want to have IAddition.getPreviousColumn
      final IInternalColumn nextColumn = addition.getNextColumn();
      int position = nextColumn.getPosition();
      html.append("between <i>" + table.getColumns().get(position - 2) + "</i> and <i>" + nextColumn + "</i>");
    }
    return html.toString();
  }

  private String omissionView(IOmission removal) {
    int position = removal.getPosition();
    return "<i>" + removal.getOmittedColumns() + "</i> at position " + (position) + " removed "+BR;
  }

  private String replacementView(IReplacement replacement) {
    int position = replacement.getPosition();
    return "<i>" + replacement.getOriginalColumns() + "</i> replaced by <i>" + replacement.getReplacementPhrase() + "</i> at position " + position+BR; // TODO
  }

  private void displayTranspositions(final IAlignment alignment) {
    final List<ITransposition> transpositions = alignment.getTranspositions();
    if (!transpositions.isEmpty()) {
      buffer.append("transpositions: ").append(BR);
      for (final ITransposition transposition : transpositions) {
        buffer.append(" ").append(transposition.toString()).append(BR);
      }
      buffer.append(BR + BR);
    }
  }

  private void displayMatches(final IAlignment alignment) {
    buffer.append("matches: ");
    String splitter = BR + " - ";
    final List<IMatch> matches = alignment.getMatches();
    for (final IMatch match : matches) {
      buffer.append(splitter).append("\"").append(match.getNormalized()).append("\"");
    }
    buffer.append(BR + BR);
  }

  public String getResult() {
    return buffer.toString();
  }
}
