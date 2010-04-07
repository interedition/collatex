package eu.interedition.collatex2.implementation.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

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

  public AlignmentTableIndex(final IAlignmentTable table, final int dummy) {
  //    Multimap<String, IPhrase> phraseMap = Multimaps.newHashMultimap();
  //    final List<INormalizedToken> tokens = witness.getTokens();
  //    for (final INormalizedToken token : tokens) {
  //      phraseMap.put(token.getNormalized(), new Phrase(Lists.newArrayList(token)));
  //    }
  //    do {
  //      final Multimap<String, IPhrase> newPhraseMap = Multimaps.newHashMultimap();
  //      //      Log.info("keys = " + phraseMap.keySet());
  //      for (final String phraseId : phraseMap.keySet()) {
  //        final Collection<IPhrase> phrases = phraseMap.get(phraseId);
  //        //        Log.info("phrases = " + phrases.toString());
  //        if (phrases.size() > 1) {
  //          addExpandedPhrases(newPhraseMap, phrases, tokens/*, phraseMap*/);
  //        } else {
  //          final IPhrase phrase = phrases.iterator().next();
  //          //          if (phrase.size() == 1) {
  //          newPhraseMap.put(phraseId, phrase);
  //          //          }
  //        }
  //        //        Log.info("newPhraseMap = " + newPhraseMap.toString());
  //        //        Log.info("");
  //      }
  //      phraseMap = newPhraseMap;
  //      //      Log.info("phraseMap.entries().size() = " + String.valueOf(phraseMap.entries().size()));
  //      //      Log.info("phraseMap.keySet().size() = " + String.valueOf(phraseMap.keySet().size()));
  //      //      Log.info("");
  //    } while (phraseMap.entries().size() > phraseMap.keySet().size());
  //    final List<IPhrase> values = Lists.newArrayList(phraseMap.values());
  //    Collections.sort(values, Phrase.PHRASECOMPARATOR);
  //    phraseBag.addAll(values);
  }

  private void addExpandedPhrases(final Multimap<String, IPhrase> newPhraseMap, final Collection<IPhrase> phrases, final List<INormalizedToken> tokens) {
    for (final IPhrase phrase : phrases) {
      final int beforePosition = phrase.getBeginPosition() - 1;
      final int afterPosition = phrase.getEndPosition();

      final INormalizedToken beforeToken = (beforePosition > 0) ? tokens.get(beforePosition - 1) : new NullToken(phrase.getBeginPosition(), phrase.getSigil());
      final INormalizedToken afterToken = (afterPosition < tokens.size()) ? tokens.get(afterPosition) : new NullToken(phrase.getEndPosition(), phrase.getSigil());

      final ArrayList<INormalizedToken> leftExpandedTokenList = Lists.newArrayList(beforeToken);
      leftExpandedTokenList.addAll(phrase.getTokens());
      final IPhrase leftExpandedPhrase = new Phrase(leftExpandedTokenList);

      final ArrayList<INormalizedToken> rightExpandedTokenList = Lists.newArrayList(phrase.getTokens());
      rightExpandedTokenList.add(afterToken);
      final IPhrase rightExpandedPhrase = new Phrase(rightExpandedTokenList);

      final String leftPhraseId = leftExpandedPhrase.getNormalized();
      newPhraseMap.put(leftPhraseId, leftExpandedPhrase);

      final String rightPhraseId = rightExpandedPhrase.getNormalized();
      newPhraseMap.put(rightPhraseId, rightExpandedPhrase);
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
