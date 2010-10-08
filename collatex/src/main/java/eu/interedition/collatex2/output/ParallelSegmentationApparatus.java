package eu.interedition.collatex2.output;

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

public class ParallelSegmentationApparatus {
  private static Logger logger = LoggerFactory.getLogger(ParallelSegmentationApparatus.class);
  
  private final List<ApparatusEntry> entries;
  private List<String> sigli;

  public List<ApparatusEntry> getEntries() {
    return entries;
  }

  public List<String> getSigli() {
    if (this.sigli == null) {
      final Set<String> sigli = Sets.newLinkedHashSet();
      for (final ApparatusEntry column : entries) {
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
  public static ParallelSegmentationApparatus build(final IAlignmentTable alignmentTable) {
    final List<ApparatusEntry> entries = Lists.newArrayList();
    ApparatusEntry mergedEntry = null;
    IColumn previousEntry = null; // Note: in the next step we have to compare
                                   // two columns with each other
    for (final IColumn column : alignmentTable.getColumns()) {
      boolean needNewCell = previousEntry == null || !previousEntry.getState().equals(column.getState()) || !column.getSigli().equals(previousEntry.getSigli());
      if (previousEntry != null && previousEntry.getState() == ColumnState.VARIANT && previousEntry.getSigli().size() > column.getSigli().size()
          && previousEntry.getSigli().containsAll(column.getSigli())) {
        needNewCell = false;
      }
      if (needNewCell) {
        final List<String> sigli = alignmentTable.getSigli();
        logger.debug("!!!" + sigli);
        mergedEntry = new ApparatusEntry(sigli);
        entries.add(mergedEntry);
      }

      final List<String> sigli = alignmentTable.getSigli();
      for (final String sigil : sigli) {
        if (column.containsWitness(sigil)) {
          final INormalizedToken token = column.getToken(sigil);
          mergedEntry.addToken(sigil, token);
        }
      }

      previousEntry = column;

    }
    return new ParallelSegmentationApparatus(entries);
  }

  private ParallelSegmentationApparatus(final List<ApparatusEntry> entries) {
    this.entries = entries;
  }
}
