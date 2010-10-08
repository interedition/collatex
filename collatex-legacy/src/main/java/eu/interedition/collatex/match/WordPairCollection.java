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

package eu.interedition.collatex.match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class WordPairCollection {
  protected final Map<String, Map<String, List<List<Word>>>> wordPairs = Maps.newHashMap();
  private final HashMap<String, Segment> witnessHash;
  public List<String> wordsInSegments;

  //  private final List<String> wordsInSegments = Lists.newArrayList();

  public WordPairCollection(final HashMap<String, Segment> _witnessHash) {
    this.witnessHash = _witnessHash;
  }

  public void addWordPair(final Word word0, final Word word1) {
    //    if (wordsInSegments.contains(wordIdentifier(word0)) || wordsInSegments.contains(wordIdentifier(word1))) return;
    //
    //    wordsInSegments.add(wordIdentifier(word0));
    //    wordsInSegments.add(wordIdentifier(word1));
    final String wordPairKey = word0._normalized + " " + word1._normalized;
    Map<String, List<List<Word>>> listMap = wordPairs.get(wordPairKey);
    if (listMap == null) {
      listMap = Maps.newHashMap();
    }
    final String witnessId = word0.getWitnessId();
    List<List<Word>> pairList = listMap.get(witnessId);
    if (pairList == null) {
      pairList = Lists.newArrayList();
    }
    pairList.add(Lists.newArrayList(word0, word1));
    listMap.put(witnessId, pairList);
    wordPairs.put(wordPairKey, listMap);
  }

  public List<WordSegment> getWordSegments(final List<String> _wordsInSegments) {
    wordsInSegments = _wordsInSegments;
    final List<WordSegment> wordSegments = Lists.newArrayList();
    final Set<Entry<String, Map<String, List<List<Word>>>>> entrySet = wordPairs.entrySet();
    for (final Entry<String, Map<String, List<List<Word>>>> entry : entrySet) {
      final String normalizedWordPair = entry.getKey();
      final Map<String, List<List<Word>>> wordPairPerWitness = entry.getValue();
      final Set<Entry<String, List<List<Word>>>> entries = wordPairPerWitness.entrySet();
      final Map<String, Map<String, List<Word>>> nextWords = Maps.newHashMap();
      for (final Entry<String, List<List<Word>>> entry2 : entries) {
        final List<List<Word>> pairList = entry2.getValue();
        for (final List<Word> pair : pairList) {
          final Word nextWord = getNextWord(pair);
          if (nextWord != null) {
            final String normalized = nextWord._normalized;
            Map<String, List<Word>> map = nextWords.get(normalized);
            if (map == null) map = Maps.newHashMap();
            pair.add(nextWord);
            map.put(entry2.getKey(), pair);
            nextWords.put(normalized, map);
          }
        }
      }
      final Set<Entry<String, Map<String, List<Word>>>> entrySet2 = nextWords.entrySet();
      for (final Entry<String, Map<String, List<Word>>> entry2 : entrySet2) {
        final String normalizedNextWord = entry2.getKey();
        final Map<String, List<Word>> pairPerWitness = entry2.getValue();
        final WordSegment wordSegment = new WordSegment(normalizedWordPair + " " + normalizedNextWord);
        if (pairPerWitness.size() > 1) {
          final Set<Entry<String, List<Word>>> entrySet3 = pairPerWitness.entrySet();
          for (final Entry<String, List<Word>> entry3 : entrySet3) {
            wordSegment.addWitnessPair(entry3.getKey(), entry3.getValue());
          }
        } else {
          //          Entry<String, List<Word>> entry3 = pairPerWitness.entrySet().iterator().next();
          //          wordSegment.addWitnessPair(entry3.getKey(), entry3.getValue());
        }
        wordSegment.grow(witnessHash, wordsInSegments);
        wordsInSegments = wordSegment.wordsInSegments;
        wordSegments.add(wordSegment);
        //        }
      }

      //      for (Entry<String, List<Word>> key_value : entries) {
      //        String witnessId = key_value.getKey();
      //        Util.p("witnessId", witnessId);
      //        List<Word> words = key_value.getValue();
      //        //        if (word)
      //      }
    }
    return wordSegments;
  }

  private Word getNextWord(final List<Word> wordList) {
    final Word lastWord = wordList.get(wordList.size() - 1);
    final Segment witness = witnessHash.get(lastWord.getWitnessId());
    final boolean witnessHasMoreWords = lastWord.position < witness.wordSize();
    final Word nextWord = witnessHasMoreWords ? witness.getElementOnWordPosition(lastWord.position + 1) : null;
    return nextWord;
  }

}
