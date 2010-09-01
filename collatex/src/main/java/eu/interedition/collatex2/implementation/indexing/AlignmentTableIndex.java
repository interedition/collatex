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

package eu.interedition.collatex2.implementation.indexing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.ITokenIndex;

public class AlignmentTableIndex implements IAlignmentTableIndex, ITokenIndex {
  private static Logger logger = LoggerFactory.getLogger(AlignmentTableIndex.class);
  
  private final Map<String, IPhrase> normalizedToPhrase;

  private AlignmentTableIndex(IAlignmentTable table) {
    this.normalizedToPhrase = Maps.newLinkedHashMap();
    for (IRow row : table.getRows()) {
      List<INormalizedToken> tokens = Lists.newArrayList(); 
      for (ICell cell : row) {
        if (!cell.isEmpty()) {
          INormalizedToken token = cell.getToken();
          tokens.add(token);
        }
      }
      // do unigram indexing
      final Multimap<String, INormalizedToken> normalizedTokenMap = ArrayListMultimap.create();
      for (final INormalizedToken token : tokens) {
        normalizedTokenMap.put(token.getNormalized(), token);
      }
      for (final String key : normalizedTokenMap.keySet()) {
        final Collection<INormalizedToken> tokenCollection = normalizedTokenMap.get(key);
        if (tokenCollection.size() == 1) {
          List<INormalizedToken> firstToken = Lists.newArrayList(normalizedTokenMap.get(key));
          normalizedToPhrase.put(key, new Phrase(firstToken));
        }
      }

      // do bigram indexing
      BiGramIndex index = BiGramIndex.create(tokens);
      List<BiGram> biGrams = index.getBiGrams();
      for (BiGram gram : biGrams) {
        normalizedToPhrase.put(gram.getNormalized(), new Phrase(Lists.newArrayList(gram.getFirstToken(), gram.getLastToken())));
      }
    }

  }

  public static IAlignmentTableIndex create(final IAlignmentTable table, final List<String> repeatingTokens) {
    final AlignmentTableIndex index = new AlignmentTableIndex(table);
//    
//      findUniquePhrasesForRow(sigil, table, index, repeatingTokens);
//    }
    return index;
  }

  @Override
  public boolean containsNormalizedPhrase(String normalized) {
    return contains(normalized);
  }

  @Override
  public int size() {
    return normalizedToPhrase.size();
  }

  @Override
  public boolean contains(String key) {
    return normalizedToPhrase.containsKey(key);
  }

  @Override
  public IPhrase getPhrase(String key) {
    return normalizedToPhrase.get(key);
  }

  @Override
  public Set<String> keys() {
    return normalizedToPhrase.keySet();
  }

 
}
