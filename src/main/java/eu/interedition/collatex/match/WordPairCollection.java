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

  public WordPairCollection(HashMap<String, Segment> _witnessHash) {
    this.witnessHash = _witnessHash;
  }

  public void addWordPair(Word word0, Word word1) {
    //    if (wordsInSegments.contains(wordIdentifier(word0)) || wordsInSegments.contains(wordIdentifier(word1))) return;
    //
    //    wordsInSegments.add(wordIdentifier(word0));
    //    wordsInSegments.add(wordIdentifier(word1));
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

  public List<WordSegment> getWordSegments(List<String> _wordsInSegments) {
    wordsInSegments = _wordsInSegments;
    List<WordSegment> wordSegments = Lists.newArrayList();
    Set<Entry<String, Map<String, List<List<Word>>>>> entrySet = wordPairs.entrySet();
    for (Entry<String, Map<String, List<List<Word>>>> entry : entrySet) {
      String normalizedWordPair = entry.getKey();
      Map<String, List<List<Word>>> wordPairPerWitness = entry.getValue();
      Set<Entry<String, List<List<Word>>>> entries = wordPairPerWitness.entrySet();
      Map<String, Map<String, List<Word>>> nextWords = Maps.newHashMap();
      for (Entry<String, List<List<Word>>> entry2 : entries) {
        List<List<Word>> pairList = entry2.getValue();
        for (List<Word> pair : pairList) {
          Word nextWord = getNextWord(pair);
          if (nextWord != null) {
            String normalized = nextWord.normalized;
            Map<String, List<Word>> map = nextWords.get(normalized);
            if (map == null) map = Maps.newHashMap();
            pair.add(nextWord);
            map.put(entry2.getKey(), pair);
            nextWords.put(normalized, map);
          }
        }
      }
      Set<Entry<String, Map<String, List<Word>>>> entrySet2 = nextWords.entrySet();
      for (Entry<String, Map<String, List<Word>>> entry2 : entrySet2) {
        String normalizedNextWord = entry2.getKey();
        Map<String, List<Word>> pairPerWitness = entry2.getValue();
        WordSegment wordSegment = new WordSegment(normalizedWordPair + " " + normalizedNextWord);
        if (pairPerWitness.size() > 1) {
          Set<Entry<String, List<Word>>> entrySet3 = pairPerWitness.entrySet();
          for (Entry<String, List<Word>> entry3 : entrySet3) {
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

  private Word getNextWord(List<Word> wordList) {
    Word lastWord = wordList.get(wordList.size() - 1);
    Segment witness = witnessHash.get(lastWord.getWitnessId());
    boolean witnessHasMoreWords = lastWord.position < witness.size();
    Word nextWord = witnessHasMoreWords ? witness.getWordOnPosition(lastWord.position + 1) : null;
    return nextWord;
  }

}
