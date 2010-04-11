package eu.interedition.collatex2.implementation.matching;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.implementation.indexing.AlignmentTableIndex;
import eu.interedition.collatex2.implementation.indexing.NullColumn;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class IndexMatcher {
  public static final Log LOG = LogFactory.getLog(IndexMatcher.class);

  public static List<IMatch> getMatchesUsingWitnessIndex(final IAlignmentTable table, final IWitness witness) {
    final List<String> repeatingTokens = IndexMatcher.combineRepeatingTokens(table, witness);
    return IndexMatcher.findMatches(AlignmentTableIndex.create(table, repeatingTokens), new WitnessIndex(witness, repeatingTokens));
  }

  public static List<String> combineRepeatingTokens(final IAlignmentTable table, final IWitness witness) {
    final Set<String> repeatingTokens = Sets.newHashSet();
    repeatingTokens.addAll(table.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    return Lists.newArrayList(repeatingTokens);
  }

  public static List<IMatch> findMatches(final IAlignmentTableIndex tableIndex, final IWitnessIndex witnessIndex) {
    final List<IMatch> matches = Lists.newArrayList();
    final Collection<IPhrase> phrases = witnessIndex.getPhrases();
    for (final IPhrase phrase : phrases) {
      if (tableIndex.containsNormalizedPhrase(phrase.getNormalized())) {
        final IColumns matchingColumns = tableIndex.getColumns(phrase.getNormalized());
        matches.add(new Match(matchingColumns, phrase));
      }
    }
    IndexMatcher.LOG.info("unfiltered matches: " + matches);
    return IndexMatcher.joinOverlappingMatches(matches);
  }

  public static List<IMatch> joinOverlappingMatches(final List<IMatch> matches) {
    final List<IMatch> newMatches = IndexMatcher.filterMatchesBasedOnPositionMatches(matches);
    IndexMatcher.LOG.info("filtered matches: " + newMatches);
    return newMatches;
  }

  //TODO: make IColumns Iterable!
  //TODO: check whether there is a wrong second token placed on the same position!
  @SuppressWarnings("boxing")
  public static List<IMatch> filterMatchesBasedOnPositionMatches(final List<IMatch> matches) {
    final Map<Integer, IColumn> columnsMap = Maps.newHashMap();
    final Map<Integer, INormalizedToken> tokenMap = Maps.newHashMap();
    for (final IMatch match : matches) {
      //TODO: rename match.getColumnsA to match.getColumns
      final IColumns columns = match.getColumns();
      final IPhrase phrase = match.getPhrase();
      final Iterator<INormalizedToken> tokens = phrase.getTokens().iterator();
      for (final IColumn column : columns.getColumns()) {
        if (!(column instanceof NullColumn)) {
          final int position = column.getPosition();
          columnsMap.put(position, column);
          tokenMap.put(position, tokens.next());
        } else {
          tokens.next();
        }
      }
    }
    final List<IMatch> newMatches = Lists.newArrayList();
    final List<Integer> positions = Lists.newArrayList(columnsMap.keySet());
    Collections.sort(positions);
    for (final Integer position : positions) {
      final IColumn column = columnsMap.get(position);
      final INormalizedToken token = tokenMap.get(position);
      //TODO: hide this in constructors!
      final IColumns columns = new Columns(Lists.newArrayList(column));
      final IPhrase phrase = new Phrase(Lists.newArrayList(token));
      final IMatch newMatch = new Match(columns, phrase);
      newMatches.add(newMatch);
    }
    return newMatches;
  }

}
