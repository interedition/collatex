package eu.interedition.collatex.parallel_segmentation;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.alignment.multiple_witness.ColumnState;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

// NOTE: this class should become obsolete if the 
// complete alignment pipeline works with segment phrases instead
// of words
public class AlignmentTableSegmentator {

  public static TeiParallelSegmentationTable createTeiParrallelSegmentationTable(final AlignmentTable2 alignmentTable) {
    return new TeiParallelSegmentationTable(AlignmentTableSegmentator.mergeColumns(alignmentTable));
  }

  // Note: to merge or not to merge? that is the question
  static List<SegmentColumn2> mergeColumns(final AlignmentTable2 alignmentTable) {
    final List<SegmentColumn2> mergedColumns = Lists.newArrayList();
    final List<Column> columns = alignmentTable.getColumns();
    SegmentColumn2 mergedColumn = null;
    Column previousColumn = null; // Note: in the next step we have to compare two columns with each other
    for (final Column column : columns) {
      boolean needNewCell = previousColumn == null || !previousColumn.getColumnState().equals(column.getColumnState()) || !column.getSigli().equals(previousColumn.getSigli());
      if (previousColumn != null && previousColumn.getColumnState() == ColumnState.VARIANT && previousColumn.getSigli().size() > column.getSigli().size()
          && previousColumn.getSigli().containsAll(column.getSigli())) {
        needNewCell = false;
      }
      if (needNewCell) {
        mergedColumn = new SegmentColumn2(alignmentTable.getWitnesses());
        mergedColumns.add(mergedColumn);
      }

      final List<Segment> witnesses = alignmentTable.getWitnesses();
      for (final Segment witness : witnesses) {
        if (column.containsWitness(witness)) {
          final Word word = (Word) column.getWord(witness);
          mergedColumn.addWord(witness, word);
        }
      }

      previousColumn = column;

    }
    return mergedColumns;
  }

}
