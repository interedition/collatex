package eu.interedition.collatex2.implementation.indexing;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class AlignmentTableIndex {
  Multimap<String, IColumn> columnsForNormalizedPhrase = Multimaps.newArrayListMultimap();

  public AlignmentTableIndex(final IAlignmentTable table) {
    final Multimap<String, IColumn> columnsForToken = Multimaps.newArrayListMultimap();
    for (final IColumn column : table.getColumns()) {
      for (final INormalizedToken normalizedToken : column.getVariants()) {
        columnsForToken.put(normalizedToken.getNormalized(), column);
      }
    }

    //
  }

  public boolean containsNormalizedPhrase(final String normalized) {
    return columnsForNormalizedPhrase.containsKey(normalized);
  }

  public IColumns getColumns(final String normalized) {
    return new Columns(Lists.newArrayList(columnsForNormalizedPhrase.get(normalized)));
  }

  public int size() {
    return columnsForNormalizedPhrase.keySet().size();
  }

}
