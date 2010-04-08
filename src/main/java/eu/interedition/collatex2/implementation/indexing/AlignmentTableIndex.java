package eu.interedition.collatex2.implementation.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class AlignmentTableIndex {
  Multimap<String, IColumn> columnsForNormalizedPhrase = Multimaps.newArrayListMultimap();
  private ColumnCells value;

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
    Multimap<String, List<ColumnCells>> phraseColumnsMap = Multimaps.newHashMultimap();
    for (final IColumn tableColumn : tableColumns) {
      final Map<String, ColumnCells> cellsForTokenMap = Maps.newHashMap();
      for (final INormalizedToken normalizedToken : tableColumn.getVariants()) {
        final String tokenName = normalizedToken.getNormalized();
        ColumnCells columnCells;
        if (cellsForTokenMap.containsKey(tokenName)) {
          columnCells = cellsForTokenMap.get(tokenName);
        } else {
          columnCells = new ColumnCells(tableColumn);
          cellsForTokenMap.put(tokenName, columnCells);
        }
        columnCells.addSigil(normalizedToken.getSigil());
      }
      for (final Entry<String, ColumnCells> entry : cellsForTokenMap.entrySet()) {
        final String tokenName = entry.getKey();
        phraseColumnsMap.put(tokenName, Lists.newArrayList(entry.getValue()));
      }
    }

    do {
      final Multimap<String, List<ColumnCells>> newPhraseColumnsMap = Multimaps.newHashMultimap();
      for (final String phraseId : phraseColumnsMap.keySet()) {
        final Collection<List<ColumnCells>> phraseColumnsCollection = phraseColumnsMap.get(phraseId);
        if (phraseColumnsCollection.size() == 1) {
          final List<ColumnCells> phraseColumns = phraseColumnsCollection.iterator().next();
          newPhraseColumnsMap.put(phraseId, phraseColumns);
        } else {
          addExpandedPhrases(newPhraseColumnsMap, phraseColumnsCollection, tableColumns, phraseId);
        }
      }
      phraseColumnsMap = newPhraseColumnsMap;
    } while (phraseColumnsMap.entries().size() > phraseColumnsMap.keySet().size());

    for (final Entry<String, List<ColumnCells>> entry : phraseColumnsMap.entries()) {
      for (final ColumnCells columnCell : entry.getValue()) {
        columnsForNormalizedPhrase.put(entry.getKey(), columnCell.getColumn());
      }
    }
  }

  //  }

  private void addExpandedPhrases(final Multimap<String, List<ColumnCells>> newPhraseColumnsMap, final Collection<List<ColumnCells>> phraseColumnsCollection, final List<IColumn> tableColumns,
      final String phraseId) {
    for (final List<ColumnCells> phraseColumns : phraseColumnsCollection) {

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
      final List<ColumnCells> leftExpandedPhrase = phraseAsListOfColumnCells(leftExpandedTokenList);

      //      final ArrayList<INormalizedToken> rightExpandedTokenList = Lists.newArrayList(phraseColumns.getTokens());
      //      rightExpandedTokenList.add(afterToken);
      //      final List<ColumnCells> rightExpandedPhrase = phraseAsListOfColumnCells(rightExpandedTokenList);

      final String leftPhraseId = getPhraseId(leftExpandedPhrase);
      newPhraseColumnsMap.put(leftPhraseId, leftExpandedPhrase);

      //      final String rightPhraseId = getPhraseId(rightExpandedPhrase);
      //      newPhraseColumnsMap.put(rightPhraseId, rightExpandedPhrase);
    }
  }

  private String getPhraseId(final List<ColumnCells> columnCellsList) {
    final List<String> tokenList = Lists.newArrayList();
    for (final ColumnCells columnCells : columnCellsList) {
      tokenList.add(columnCells.getNormalized());
    }
    return Join.join(" ", tokenList);
  }

  private List<ColumnCells> phraseAsListOfColumnCells(final ArrayList<INormalizedToken> tokenList) {
    return null;
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

  private static class ColumnCells {
    private final List<String> sigli;
    private final IColumn column;

    public ColumnCells(final IColumn column1) {
      this.column = column1;
      this.sigli = Lists.newArrayList();
    }

    public String getNormalized() {
      return column.getToken(sigli.get(0)).getNormalized();
    }

    public IColumn getColumn() {
      return column;
    }

    public void addSigil(final String sigil) {
      sigli.add(sigil);
    }

    public List<String> getSigli() {
      return sigli;
    }
  }

}
