package eu.interedition.collatex.output;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.views.Element;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.superbase.AlignmentTable2;
import eu.interedition.collatex.superbase.Column;

public class AppAlignmentTable {

  private final AlignmentTable2 alignmentTable;
  private final List<Element> cells;

  public AppAlignmentTable(AlignmentTable2 _alignmentTable) {
    this.alignmentTable = _alignmentTable;
    this.cells = Lists.newArrayList();

    mergeColumns();
  }

  // Note: to merge or not to merge? that is the question
  private void mergeColumns() {
    List<Column> columns = alignmentTable.getColumns();
    AppElementTEI mergedColumn = null;
    Column previousColumn = null; // Note: in the next step we have to compare two columns with each other
    for (Column column : columns) {
      boolean needNewCell = previousColumn == null || !previousColumn.getColumnState().equals(column.getColumnState()) || !column.getSigli().equals(previousColumn.getSigli());
      if (needNewCell) {
        mergedColumn = new AppElementTEI(this, null, null);
        cells.add(mergedColumn);
      }

      List<Witness> witnesses = alignmentTable.getWitnesses();
      for (Witness witness : witnesses) {
        if (column.containsWitness(witness)) {
          Word word = column.getWord(witness);
          mergedColumn.addWord(witness, word);
        }
      }

      previousColumn = column;

    }
  }

  //  public AppAlignmentTable(Collation _matchNonMatch) {
  //    // FIXME we should decide somewhere _which_ of the permutations we chose. 
  //    this.matchNonMatch = _matchNonMatch;
  //    cells = new Element[100]; // FIXME use maximum number of words per witness -- but need witness here
  //
  //    for (Match match : matchNonMatch.getMatches()) {
  //      Word matchedWord = match.getBaseWord();
  //      cells[matchedWord.position * 2 + 1] = new TextElement(matchedWord);
  //    }
  //
  //    for (Gap nonMatch : matchNonMatch.getGaps()) {
  //      // FIXME somehow propagate Bram's witness id here after merge
  //      cells[nonMatch.getPhraseA().getStartPosition() * 2] = new AppElementTEI(nonMatch.getPhraseA(), nonMatch.getPhraseB());
  //    }
  //
  //  }

  public String toXML() {
    StringBuilder result = new StringBuilder(); // FIXME initialize length
    result.append("<collation>");
    String delimiter = "";
    for (Element cell : cells) {
      if (cell != null) {
        result.append(delimiter); // FIXME can we just introduce whitespace here!?
        result.append(cell.toXML());
        delimiter = " ";
      }
    }

    result.append("</collation>");

    return result.toString();
  }

  public List<Witness> getWitnesses() {
    return alignmentTable.getWitnesses();
  }

}
