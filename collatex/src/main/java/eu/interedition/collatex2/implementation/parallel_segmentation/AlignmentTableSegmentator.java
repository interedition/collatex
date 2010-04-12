package eu.interedition.collatex2.implementation.parallel_segmentation;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class AlignmentTableSegmentator {
  public static ParallelSegmentationTable createParrallelSegmentationTable(final IAlignmentTable alignmentTable) {
    return new ParallelSegmentationTable(AlignmentTableSegmentator.mergeColumns(alignmentTable));
  }

  // Note: to merge or not to merge? that is the question
  static List<SegmentColumn> mergeColumns(final IAlignmentTable alignmentTable) {
    final List<SegmentColumn> mergedColumns = Lists.newArrayList();
    final List<IColumn> columns = alignmentTable.getColumns();
    SegmentColumn mergedColumn = null;
    IColumn previousColumn = null; // Note: in the next step we have to compare two columns with each other
    for (final IColumn column : columns) {
      boolean needNewCell = previousColumn == null || !previousColumn.getState().equals(column.getState()) || !column.getSigli().equals(previousColumn.getSigli());
      if (previousColumn != null && previousColumn.getState() == ColumnState.VARIANT && previousColumn.getSigli().size() > column.getSigli().size()
          && previousColumn.getSigli().containsAll(column.getSigli())) {
        needNewCell = false;
      }
      if (needNewCell) {
        final List<String> sigli = alignmentTable.getSigli();
        System.out.println("!!!" + sigli);
        mergedColumn = new SegmentColumn(sigli);
        mergedColumns.add(mergedColumn);
      }

      final List<String> sigli = alignmentTable.getSigli();
      for (final String sigil : sigli) {
        if (column.containsWitness(sigil)) {
          final INormalizedToken token = column.getToken(sigil);
          mergedColumn.addToken(sigil, token);
        }
      }

      previousColumn = column;

    }
    return mergedColumns;
  }
}
