package eu.interedition.collatex.parallel_segmentation;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.alignment.multiple_witness.ColumnState;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class TeiParallelSegmentationTable {

  // TODO: rename cells to _columns!
  private final List<SegmentColumn2> cells;
  private final List<Witness> _witnesses;

  // TODO: make this a static constructor!
  public TeiParallelSegmentationTable(AlignmentTable2 alignmentTable) {
    this._witnesses = alignmentTable.getWitnesses();
    this.cells = mergeColumns(alignmentTable);
  }

  // Note: to merge or not to merge? that is the question
  private static List<SegmentColumn2> mergeColumns(AlignmentTable2 alignmentTable) {
    List<SegmentColumn2> mergedColumns = Lists.newArrayList();
    List<Column> columns = alignmentTable.getColumns();
    SegmentColumn2 mergedColumn = null;
    Column previousColumn = null; // Note: in the next step we have to compare two columns with each other
    for (Column column : columns) {
      boolean needNewCell = previousColumn == null || !previousColumn.getColumnState().equals(column.getColumnState()) || !column.getSigli().equals(previousColumn.getSigli());
      if (previousColumn != null && previousColumn.getColumnState() == ColumnState.VARIANT && previousColumn.getSigli().size() > column.getSigli().size()
          && previousColumn.getSigli().containsAll(column.getSigli())) {
        needNewCell = false;
      }
      if (needNewCell) {
        mergedColumn = new SegmentColumn2(alignmentTable.getWitnesses(), null, null);
        mergedColumns.add(mergedColumn);
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
    return mergedColumns;
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

  // TODO: rename cell here!
  public String toXML() {
    StringBuilder result = new StringBuilder(); // FIXME initialize length
    result.append("<collation>");
    String delimiter = "";
    for (SegmentColumn2 cell : cells) {
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
    return _witnesses;
  }

}
