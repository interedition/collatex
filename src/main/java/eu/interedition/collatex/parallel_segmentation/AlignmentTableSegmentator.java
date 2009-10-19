package eu.interedition.collatex.parallel_segmentation;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.alignment.multiple_witness.ColumnState;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class AlignmentTableSegmentator {

  public static TeiParallelSegmentationTable createTeiParrallelSegmentationTable(AlignmentTable2 alignmentTable) {
    return new TeiParallelSegmentationTable(AlignmentTableSegmentator.mergeColumns(alignmentTable));
  }

  // Note: to merge or not to merge? that is the question
  static List<SegmentColumn2> mergeColumns(AlignmentTable2 alignmentTable) {
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
        mergedColumn = new SegmentColumn2(alignmentTable.getWitnesses());
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

}
