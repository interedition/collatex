package eu.interedition.collatex2.implementation.indexing;

import java.util.Collection;
import java.util.List;

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
    final List<IColumn> tableColumns = table.getColumns();
    for (final IColumn column : tableColumns) {
      for (final INormalizedToken normalizedToken : column.getVariants()) {
        columnsForToken.put(normalizedToken.getNormalized(), column);
      }
    }
    for (final String tokenName : columnsForToken.keySet()) {
      final Collection<IColumn> columns = columnsForToken.get(tokenName);
      if (columns.size() > 1) {
        for (final IColumn column : columns) {
          final int position = column.getPosition();
          for (final INormalizedToken normalizedToken : column.getVariants()) {
            final IColumn beforeColumn;
            if (position == 0) {
              beforeColumn = new NullColumn();
            } else {
              beforeColumn = tableColumns.get(position - 1);
            }
            if (normalizedToken.equals(tokenName)) {

            }
          }
        }
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
