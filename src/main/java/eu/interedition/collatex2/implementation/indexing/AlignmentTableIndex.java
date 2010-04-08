package eu.interedition.collatex2.implementation.indexing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

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
          final IColumn beforeColumn;
          if (position == 0) {
            beforeColumn = new NullColumn();
          } else {
            beforeColumn = tableColumns.get(position - 1);
          }
          for (final INormalizedToken normalizedToken : column.getVariants()) {
            if (normalizedToken.equals(tokenName)) {

            }
          }
        }
      } else {
        columnsForNormalizedPhrase.put(tokenName, columns.iterator().next());
      }

    }

    //
  }

  Multiset<IColumns> phraseBag = Multisets.newTreeMultiset();

  public AlignmentTableIndex(final IAlignmentTable table, final int dummy) {
    Multimap<String, IColumns> columnsMap = Multimaps.newHashMultimap();
    final List<IColumn> tableColumns = table.getColumns();
    for (final IColumn tableColumn : tableColumns) {
      for (final INormalizedToken normalizedToken : tableColumn.getVariants()) {
        columnsMap.put(normalizedToken.getNormalized(), new Columns(Lists.newArrayList(tableColumn)));
      }
    }
    do {
      final Multimap<String, IColumns> newPhraseMap = Multimaps.newHashMultimap();
      //      Log.info("keys = " + phraseMap.keySet());
      for (final String phraseId : columnsMap.keySet()) {
        final Collection<IColumns> phraseColumns = columnsMap.get(phraseId);
        //        Log.info("phrases = " + phrases.toString());
        if (phraseColumns.size() > 1) {
          addExpandedPhrases(newPhraseMap, phraseColumns, tableColumns, phraseId /*, phraseMap*/);
        } else {
          final IColumns phrase = phraseColumns.iterator().next();
          //          if (phrase.size() == 1) {
          newPhraseMap.put(phraseId, phrase);
          //          }
        }
        //        Log.info("newPhraseMap = " + newPhraseMap.toString());
        //        Log.info("");
      }
      columnsMap = newPhraseMap;
      //      Log.info("phraseMap.entries().size() = " + String.valueOf(phraseMap.entries().size()));
      //      Log.info("phraseMap.keySet().size() = " + String.valueOf(phraseMap.keySet().size()));
      //      Log.info("");
    } while (columnsMap.entries().size() > columnsMap.keySet().size());
    final List<IColumns> values = Lists.newArrayList(columnsMap.values());
    Collections.sort(values, Columns.COLUMNSCOMPARATOR);
    phraseBag.addAll(values);
  }

  private void addExpandedPhrases(final Multimap<String, IColumns> newPhraseMap, final Collection<IColumns> phrases, final List<IColumn> tableColumns, final String phraseId) {
    for (final IColumns phraseColumn : phrases) {
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
