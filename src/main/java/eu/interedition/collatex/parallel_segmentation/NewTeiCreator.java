package eu.interedition.collatex.parallel_segmentation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.input.Phrase;

public class NewTeiCreator {
  public static TeiParallelSegmentationTable createTEI(final AlignmentTable2 table) {
    final List<Column> columns = table.getColumns();
    final List<SegmentColumn2> segcol = Lists.newArrayList();
    for (final Column column : columns) {

      final SegmentColumn2 newColumn = new SegmentColumn2(table.getSigli());
      final Set<String> sigli = column.getSigli();
      for (final String sigil : sigli) {
        final Phrase phrase = (Phrase) column.getWord(sigil);
        newColumn.addPhrase(sigil, phrase);
      }
      segcol.add(newColumn);
    }
    final TeiParallelSegmentationTable tei = new TeiParallelSegmentationTable(segcol);
    return tei;
  }
}
