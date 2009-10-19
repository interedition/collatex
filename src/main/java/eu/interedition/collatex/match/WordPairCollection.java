package eu.interedition.collatex.match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class WordPairCollection {
  protected final Map<String, Map<String, List<List<Word>>>> wordPairs = Maps.newHashMap();
  private final HashMap<String, Witness> witnessHash;

  public WordPairCollection(HashMap<String, Witness> witnessHash) {
    this.witnessHash = witnessHash;
  }

  public void addWordPair(Word word0, Word word1) {
    String wordPairKey = word0.normalized + " " + word1.normalized;
    Map<String, List<List<Word>>> listMap = wordPairs.get(wordPairKey);
    if (listMap == null) {
      listMap = Maps.newHashMap();
    }
    String witnessId = word0.getWitnessId();
    List<List<Word>> pairList = listMap.get(witnessId);
    if (pairList == null) {
      pairList = Lists.newArrayList();
    }
    pairList.add(Lists.newArrayList(word0, word1));
    listMap.put(witnessId, pairList);
    wordPairs.put(wordPairKey, listMap);
  }

  public List<WordSegment> getWordSegments() {
    List<WordSegment> wordSegments = Lists.newArrayList();
    Set<Entry<String, Map<String, List<List<Word>>>>> entrySet = wordPairs.entrySet();
    for (Entry<String, Map<String, List<List<Word>>>> entry : entrySet) {
      String wordPairKey = entry.getKey();
      Util.p("wordPairKey", wordPairKey);
      Map<String, List<List<Word>>> wordPairPerWitness = entry.getValue();
      Set<Entry<String, List<List<Word>>>> entries = wordPairPerWitness.entrySet();
      Map<String, Map<String, List<Word>>> nextWords = Maps.newHashMap();
      for (Entry<String, List<List<Word>>> entry2 : entries) {
        List<List<Word>> pairList = entry2.getValue();
        for (List<Word> pair : pairList) {
          Word nextWord = getNextWord(pair, witnessHash);
          if (nextWord != null) {
            String normalized = nextWord.normalized;
            Map<String, List<Word>> map = nextWords.get(normalized);
            if (map == null) map = Maps.newHashMap();
            map.put(entry2.getKey(), pair);
            nextWords.put(normalized, map);
          }
        }
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

  private Word getNextWord(List<Word> wordList, HashMap<String, Witness> witnessHash) {
    Word lastWord = wordList.get(wordList.size() - 1);
    Witness witness = witnessHash.get(lastWord.getWitnessId());
    boolean witnessHasMoreWords = lastWord.position < witness.size();
    Word nextWord = witnessHasMoreWords ? witness.getWordOnPosition(lastWord.position + 1) : null;
    return nextWord;
  }

}
