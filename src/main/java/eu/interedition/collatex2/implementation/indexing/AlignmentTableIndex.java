package eu.interedition.collatex2.implementation.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

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
    final List<IColumn> tableColumns = table.getColumns();

    //    if (false) {
    //      // try 1
    //      final Multimap<String, IColumn> columnsForToken = Multimaps.newArrayListMultimap();
    //      for (final IColumn column : tableColumns) {
    //        for (final INormalizedToken normalizedToken : column.getVariants()) {
    //          columnsForToken.put(normalizedToken.getNormalized(), column);
    //        }
    //      }
    //      for (final String tokenName : columnsForToken.keySet()) {
    //        final Collection<IColumn> columns = columnsForToken.get(tokenName);
    //        if (columns.size() > 1) {
    //          for (final IColumn column : columns) {
    //            final int position = column.getPosition();
    //            final IColumn beforeColumn;
    //            if (position == 0) {
    //              beforeColumn = new NullColumn();
    //            } else {
    //              beforeColumn = tableColumns.get(position - 1);
    //            }
    //            for (final INormalizedToken normalizedToken : column.getVariants()) {
    //              if (normalizedToken.equals(tokenName)) {
    //                // what?
    //              }
    //            }
    //          }
    //        } else {
    //          columnsForNormalizedPhrase.put(tokenName, columns.iterator().next());
    //        }
    //
    //      }
    //    } else {
    //
    //      // try 2
    Multimap<String, List<ColumnCell>> phraseColumnsMap = Multimaps.newHashMultimap();
    for (final IColumn tableColumn : tableColumns) {
      for (final INormalizedToken normalizedToken : tableColumn.getVariants()) {
        phraseColumnsMap.put(normalizedToken.getNormalized(), Lists.newArrayList(new ColumnCell(tableColumn, normalizedToken.getSigil())));
      }
    }

    do {
      final Multimap<String, List<ColumnCell>> newPhraseColumnsMap = Multimaps.newHashMultimap();
      for (final String phraseId : phraseColumnsMap.keySet()) {
        final Collection<List<ColumnCell>> phraseColumnsCollection = phraseColumnsMap.get(phraseId);
        if (phraseColumnsCollection.size() == 1) {
          final List<ColumnCell> phraseColumns = phraseColumnsCollection.iterator().next();
          newPhraseColumnsMap.put(phraseId, phraseColumns);
        } else {
          addExpandedPhrases(newPhraseColumnsMap, phraseColumnsCollection, tableColumns, phraseId /*, phraseMap*/);
        }
      }
      phraseColumnsMap = newPhraseColumnsMap;
    } while (phraseColumnsMap.entries().size() > phraseColumnsMap.keySet().size());

    for (final Entry<String, List<ColumnCell>> entry : phraseColumnsMap.entries()) {
      final List<ColumnCell> value = entry.getValue();
      for (final ColumnCell columnCell : value) {
        columnsForNormalizedPhrase.put(entry.getKey(), columnCell.getColumn());
      }
    }
  }

  //  }

  private void addExpandedPhrases(final Multimap<String, List<ColumnCell>> newPhraseColumnsMap, final Collection<List<ColumnCell>> phraseColumnsCollection, final List<IColumn> tableColumns,
      final String phraseId) {
    for (final List<ColumnCell> phraseColumns : phraseColumnsCollection) {

      final int phraseColumnsBeginPosition = phraseColumns.get(0).getColumn().getPosition();
      final int phraseColumnsEndPosition = phraseColumns.get(phraseColumns.size() - 1).getColumn().getPosition();
      final int beforePosition = phraseColumnsBeginPosition - 1;
      final int afterPosition = phraseColumnsEndPosition + 1;

      final IColumn beforeColumn = (beforePosition > 0) ? tableColumns.get(beforePosition - 1) : new NullColumn(phraseColumnsBeginPosition);
      final INormalizedToken beforeToken = beforeColumn.getVariants().get(0);
      final IColumn afterColumn = (afterPosition < tableColumns.size()) ? tableColumns.get(afterPosition) : new NullColumn(phraseColumnsEndPosition);
      final INormalizedToken afterToken = afterColumn.getVariants().get(0);

      final ArrayList<INormalizedToken> leftExpandedTokenList = Lists.newArrayList(beforeToken);
      //      leftExpandedTokenList.addAll(phraseColumns.getTokens());
      //      final IPhrase leftExpandedPhrase = new Phrase(leftExpandedTokenList);
      //
      //      final ArrayList<INormalizedToken> rightExpandedTokenList = Lists.newArrayList(phraseColumns.getTokens());
      //      rightExpandedTokenList.add(afterToken);
      //      final IPhrase rightExpandedPhrase = new Phrase(rightExpandedTokenList);
      //
      //      final String leftPhraseId = leftExpandedPhrase.getNormalized();
      //      newPhraseMap.put(leftPhraseId, leftExpandedPhrase);
      //
      //      final String rightPhraseId = rightExpandedPhrase.getNormalized();
      //      newPhraseMap.put(rightPhraseId, rightExpandedPhrase);
    }
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

  private static class ColumnCell {
    private final String sigil;
    private final IColumn column;

    public ColumnCell(final IColumn column1, final String sigil1) {
      this.column = column1;
      this.sigil = sigil1;
    }

    public IColumn getColumn() {
      return column;
    }

    public String getSigil() {
      return sigil;
    }
  }

}
