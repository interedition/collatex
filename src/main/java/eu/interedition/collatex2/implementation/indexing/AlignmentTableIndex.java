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
    Multimap<String, IColumn> columnsMap = Multimaps.newHashMultimap();
    for (final IColumn tableColumn : tableColumns) {
      for (final INormalizedToken normalizedToken : tableColumn.getVariants()) {
        columnsMap.put(normalizedToken.getNormalized(), tableColumn);
      }
    }
    do {
      final Multimap<String, IColumn> newPhraseMap = Multimaps.newHashMultimap();
      for (final String phraseId : columnsMap.keySet()) {
        final Collection<IColumn> phraseColumns = columnsMap.get(phraseId);
        if (phraseColumns.size() > 1) {
          addExpandedPhrases(newPhraseMap, phraseColumns, tableColumns, phraseId /*, phraseMap*/);
        } else {
          final IColumn phrase = phraseColumns.iterator().next();
          newPhraseMap.put(phraseId, phrase);
        }
      }
      columnsMap = newPhraseMap;
    } while (columnsMap.entries().size() > columnsMap.keySet().size());

    for (final java.util.Map.Entry<String, IColumn> entry : columnsMap.entries()) {
      columnsForNormalizedPhrase.put(entry.getKey(), entry.getValue());
    }
  }

  //  }

  private void addExpandedPhrases(final Multimap<String, IColumn> newPhraseMap, final Collection<IColumn> phrases, final List<IColumn> tableColumns, final String phraseId) {
    for (final IColumn phraseColumn : phrases) {
      // column heeft witnesses, welke witnesses zijn relevant?

      //      final int beforePosition = phraseColumn.getBeginPosition() - 1;
      //      final int afterPosition = phraseColumn.getEndPosition();
      //
      //      final INormalizedToken beforeToken = (beforePosition > 0) ? tableColumns.get(beforePosition - 1) : new NullToken(phraseColumn.getBeginPosition(), phraseColumn.getSigil());
      //      final INormalizedToken afterToken = (afterPosition < tableColumns.size()) ? tableColumns.get(afterPosition) : new NullToken(phraseColumn.getEndPosition(), phraseColumn.getSigil());
      //
      //      final ArrayList<INormalizedToken> leftExpandedTokenList = Lists.newArrayList(beforeToken);
      //      leftExpandedTokenList.addAll(phraseColumn.getTokens());
      //      final IPhrase leftExpandedPhrase = new Phrase(leftExpandedTokenList);
      //
      //      final ArrayList<INormalizedToken> rightExpandedTokenList = Lists.newArrayList(phraseColumn.getTokens());
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

}
