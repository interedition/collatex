/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.implementation.matching;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.implementation.indexing.AlignmentTableIndex;
import eu.interedition.collatex2.implementation.indexing.NullColumn;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.input.Phrase;
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
  public static final Logger LOG = LoggerFactory.getLogger(IndexMatcher.class);

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
    IndexMatcher.LOG.debug("unfiltered matches: " + matches);
    return IndexMatcher.joinOverlappingMatches(matches);
  }

  public static List<IMatch> joinOverlappingMatches(final List<IMatch> matches) {
    final List<IMatch> newMatches = IndexMatcher.filterMatchesBasedOnPositionMatches(matches);
    IndexMatcher.LOG.debug("filtered matches: " + newMatches);
    return newMatches;
  }

  //TODO: make IColumns Iterable!
  //NOTE: There is a potential situation here where 1 column matches with multiple phrases
  //NOTE: The other phrases are seen as additions, which causes too many empty columns
  //NOTE: --> not the optimal alignment
  @SuppressWarnings("boxing")
  public static List<IMatch> filterMatchesBasedOnPositionMatches(final List<IMatch> matches) {
    final Map<INormalizedToken, IColumn> tokenToColumn = Maps.newLinkedHashMap();
    final Map<Integer, IColumn> columnsMap = Maps.newHashMap();
    final Map<Integer, INormalizedToken> tokenMap = Maps.newHashMap();
    for (final IMatch match : matches) {
      // check whether this match has an alternative that is equal in weight
      // if so, then skip the alternative!
      // NOTE: multiple columns match with the same token!
      // step 1. Gather data
      List<ColumnToken> things = Lists.newArrayList();
      final IColumns columns = match.getColumns();
      final IPhrase phrase = match.getPhrase();
      final Iterator<INormalizedToken> tokens = phrase.getTokens().iterator();
      for (final IColumn column : columns.getColumns()) {
        final INormalizedToken token = tokens.next();
        // skip NullColumn and NullToken
        if (!(column instanceof NullColumn)) {
          things.add(new ColumnToken(column, token));
        }
      }
      // step 2. Look for alternative
      boolean foundAlternative = false;
      for (ColumnToken thing : things) {
        // check for alternative here!
        final IColumn column = thing.column;
        final INormalizedToken token = thing.token;
        if (tokenToColumn.containsKey(token)) {
          IColumn existingColumn = tokenToColumn.get(token);
          if (existingColumn != column) {
            foundAlternative = true;
          }  
        }
      }
      // step 3. Decide what to do
      if (foundAlternative) {
        IndexMatcher.LOG.debug("Phrase '"+phrase+"' is an alternative! skipping...");
      } else {
        for (ColumnToken thing : things) {
          final IColumn column = thing.column;
          final int position = column.getPosition();
          final INormalizedToken token = thing.token;
          tokenToColumn.put(token, column);
          columnsMap.put(position, column);
          tokenMap.put(position, token);
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
