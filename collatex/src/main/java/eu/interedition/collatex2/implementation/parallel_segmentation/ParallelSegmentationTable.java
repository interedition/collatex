package eu.interedition.collatex2.implementation.parallel_segmentation;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class ParallelSegmentationTable {
  private static Logger logger = LoggerFactory.getLogger(ParallelSegmentationTable.class);
  
  private final List<SegmentColumn> segmentColumns;
  private List<String> sigli;

  public ParallelSegmentationTable(final List<SegmentColumn> segmentColumns) {
    this.segmentColumns = segmentColumns;
  }

  public List<SegmentColumn> getColumns() {
    return segmentColumns;
  }

  public List<String> getSigli() {
    if (this.sigli == null) {
      final Set<String> sigli = Sets.newLinkedHashSet();
      for (final SegmentColumn column : segmentColumns) {
        sigli.addAll(column.getSigli());
      }
      this.sigli = Lists.newArrayList(sigli);
    }
    return this.sigli;
  }

  /**
   * To merge or not to merge; that is the question.
   * 
   * @param alignmentTable
   * @return
   */
  public static ParallelSegmentationTable build(final IAlignmentTable alignmentTable) {
    final List<SegmentColumn> mergedColumns = Lists.newArrayList();
    SegmentColumn mergedColumn = null;
    IColumn previousColumn = null; // Note: in the next step we have to compare
                                   // two columns with each other
    for (final IColumn column : alignmentTable.getColumns()) {
      boolean needNewCell = previousColumn == null || !previousColumn.getState().equals(column.getState()) || !column.getSigli().equals(previousColumn.getSigli());
      if (previousColumn != null && previousColumn.getState() == ColumnState.VARIANT && previousColumn.getSigli().size() > column.getSigli().size()
          && previousColumn.getSigli().containsAll(column.getSigli())) {
        needNewCell = false;
      }
      if (needNewCell) {
        final List<String> sigli = alignmentTable.getSigli();
        logger.debug("!!!" + sigli);
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
    return new ParallelSegmentationTable(mergedColumns);
  }

}
