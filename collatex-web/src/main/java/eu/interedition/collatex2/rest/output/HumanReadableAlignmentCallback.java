package eu.interedition.collatex2.rest.output;

import java.util.List;

import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IOmission;
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
    final List<IGap> gaps = alignment.getGaps();
    buffer.append("gaps: ").append(BR);
    for (final IGap gap : gaps) {
      buffer.append(" ").append(gap.toString()).append(BR);
    }
    buffer.append(BR + BR);
    displayTranspositions(alignment);
    buffer.append(BR + BR);
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
      final IColumn nextColumn = addition.getNextColumn();
      int position = nextColumn.getPosition();
      html.append("between <i>" + table.getColumns().get(position - 2) + "</i> and <i>" + nextColumn + "</i>");
    }
    return html.toString();
  }
  
  private String omissionView(IOmission removal) {
    int position = removal.getPosition();
    return "<i>" + removal.getOmittedColumns() + "</i> at position " + (position) + " removed ";
  }


  private void displayTranspositions(final IAlignment alignment) {
    final List<ITransposition> transpositions = alignment.getTranspositions();
    buffer.append("transpositions: ").append(BR);
    if (transpositions.isEmpty()) {
      buffer.append("none").append(BR);
    }
    for (final ITransposition transposition : transpositions) {
      buffer.append(" ").append(transposition.toString()).append(BR);
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