package eu.interedition.collatex2.implementation.indexing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.interfaces.IAligmentTableIndex;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class AlignmentTableIndex implements IAligmentTableIndex {
  Map<String, IColumns> columnsForNormalizedPhrase;

  public AlignmentTableIndex(final IAlignmentTable table) {
    final List<IColumn> tableColumns = table.getColumns();

    final Multimap<String, ColumnPhrase> seedlings = seed(tableColumns);
    final Map<String, ColumnPhrase> crop = grow(seedlings, tableColumns);
    columnsForNormalizedPhrase = harvest(crop);
  }

  /* (non-Javadoc)
   * @see eu.interedition.collatex2.implementation.indexing.IAligmentTableIndex#containsNormalizedPhrase(java.lang.String)
   */
  public boolean containsNormalizedPhrase(final String normalized) {
    return columnsForNormalizedPhrase.containsKey(normalized);
  }

  /* (non-Javadoc)
   * @see eu.interedition.collatex2.implementation.indexing.IAligmentTableIndex#getColumns(java.lang.String)
   */
  public IColumns getColumns(final String normalized) {
    return columnsForNormalizedPhrase.get(normalized);
  }

  /* (non-Javadoc)
   * @see eu.interedition.collatex2.implementation.indexing.IAligmentTableIndex#size()
   */
  public int size() {
    return columnsForNormalizedPhrase.keySet().size();
  }

  private Multimap<String, ColumnPhrase> seed(final List<IColumn> tableColumns) {
    final Multimap<String, ColumnPhrase> columnPhraseMap = Multimaps.newHashMultimap();

    for (final IColumn tableColumn : tableColumns) {
      final Multimap<String, String> sigliForTokenMap = Multimaps.newHashMultimap();
      for (final INormalizedToken normalizedToken : tableColumn.getVariants()) {
        final String tokenName = normalizedToken.getNormalized();
        sigliForTokenMap.put(tokenName, normalizedToken.getSigil());
      }

      for (final String tokenName : sigliForTokenMap.keySet()) {
        final Columns _columns = new Columns(Lists.newArrayList(tableColumn));
        final Collection<String> _sigli = sigliForTokenMap.get(tokenName);
        final ColumnPhrase columnPhrase = new ColumnPhrase(tokenName, _columns, _sigli);
        columnPhraseMap.put(tokenName, columnPhrase);
      }
    }
    return columnPhraseMap;
  }

  private Map<String, ColumnPhrase> grow(final Multimap<String, ColumnPhrase> seed, final List<IColumn> tableColumns) {
    Multimap<String, ColumnPhrase> columnPhraseMap = seed;

    do {
      final Multimap<String, ColumnPhrase> newColumnPhraseMap = Multimaps.newHashMultimap();
      for (final String phraseId : columnPhraseMap.keySet()) {
        final Collection<ColumnPhrase> columnPhraseCollection = columnPhraseMap.get(phraseId);
        if (columnPhraseCollection.size() == 1) {
          final ColumnPhrase phraseColumns = columnPhraseCollection.iterator().next();
          newColumnPhraseMap.put(phraseId, phraseColumns);
        } else {
          addExpandedPhrases(newColumnPhraseMap, columnPhraseCollection, tableColumns, phraseId);
        }
      }
      columnPhraseMap = newColumnPhraseMap;
    } while (columnPhraseMap.entries().size() > columnPhraseMap.keySet().size());

    final Map<String, ColumnPhrase> crop = Maps.newHashMap();
    for (final Entry<String, ColumnPhrase> entry : columnPhraseMap.entries()) {
      crop.put(entry.getKey(), entry.getValue());
    }

    return crop;
  }

  private void addExpandedPhrases(final Multimap<String, ColumnPhrase> newPhraseColumnsMap, final Collection<ColumnPhrase> phraseColumnsCollection, final List<IColumn> tableColumns,
      final String phraseId) {
  //    for (final ColumnPhrase phraseColumns : phraseColumnsCollection) {
  //
  //      final int phraseColumnsBeginPosition = phraseColumns.get(0).getColumn().getPosition();
  //      final int phraseColumnsEndPosition = phraseColumns.get(phraseColumns.size() - 1).getColumn().getPosition();
  //      final int beforePosition = phraseColumnsBeginPosition - 1;
  //      final int afterPosition = phraseColumnsEndPosition + 1;
  //
  //      final IColumn beforeColumn = (beforePosition > 0) ? tableColumns.get(beforePosition - 1) : new NullColumn(phraseColumnsBeginPosition);
  //      final INormalizedToken beforeToken = beforeColumn.getVariants().get(0);
  //      final IColumn afterColumn = (afterPosition < tableColumns.size()) ? tableColumns.get(afterPosition) : new NullColumn(phraseColumnsEndPosition);
  //      final INormalizedToken afterToken = afterColumn.getVariants().get(0);
  //
  //      final ArrayList<INormalizedToken> leftExpandedTokenList = Lists.newArrayList(beforeToken);
  //      //      leftExpandedTokenList.addAll(phraseColumns.getTokens());
  //      final ColumnPhrase leftExpandedPhrase = phraseAsListOfColumnPhrase(leftExpandedTokenList);
  //
  //      //      final ArrayList<INormalizedToken> rightExpandedTokenList = Lists.newArrayList(phraseColumns.getTokens());
  //      //      rightExpandedTokenList.add(afterToken);
  //      //      final ColumnPhrase rightExpandedPhrase = phraseAsListOfColumnPhrase(rightExpandedTokenList);
  //
  //      final String leftPhraseId = leftExpandedPhrase.getNormalized();
  //      newPhraseColumnsMap.put(leftPhraseId, leftExpandedPhrase);
  //
  //      //      final String rightPhraseId = getPhraseId(rightExpandedPhrase);
  //      //      newPhraseColumnsMap.put(rightPhraseId, rightExpandedPhrase);
  //    }
  }

  private Map<String, IColumns> harvest(final Map<String, ColumnPhrase> crop) {
    final Map<String, IColumns> harvestMap = Maps.newHashMap();
    for (final Entry<String, ColumnPhrase> entry : crop.entrySet()) {
      harvestMap.put(entry.getKey(), entry.getValue().getColumns());
    }
    return harvestMap;
  }

}
