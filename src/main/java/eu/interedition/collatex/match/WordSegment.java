package eu.interedition.collatex.match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class WordSegment {

  public String title;
  private final Map<String, List<Word>> wordsPerWitness = Maps.newHashMap();

  public WordSegment(String _title) {
    this.title = _title;
  }

  public void addWitness(String witnessId, List<Word> words) {
    wordsPerWitness.put(witnessId, words);
  }

  @Override
  public String toString() {
    return title;
  }

  public void grow(HashMap<String, Segment> witnessHash) {
    boolean nextWordsMatch = true;
    while (nextWordsMatch) {
      Set<String> nextWordSet = Sets.newHashSet();
      Map<String, Word> nextWords = Maps.newHashMap();
      for (List<Word> wordList : wordsPerWitness.values()) {
        Word nextWord = getNextWord(wordList, witnessHash);
        if (nextWord == null) {
          nextWordSet.add(null);
        } else {
          nextWords.put(nextWord.getWitnessId(), nextWord);
          nextWordSet.add(nextWord.normalized);
        }
      }
      nextWordsMatch = (nextWordSet.size() == 1 && !nextWordSet.contains(null));
      if (nextWordsMatch) {
        for (Entry<String, List<Word>> entry : wordsPerWitness.entrySet()) {
          entry.getValue().add(nextWords.get(entry.getKey()));
        }
        this.title += " " + nextWordSet.iterator().next();
      }
    }

  }

  private Word getNextWord(List<Word> wordList, HashMap<String, Segment> witnessHash) {
    Word lastWord = wordList.get(wordList.size() - 1);
    Segment witness = witnessHash.get(lastWord.getWitnessId());
    boolean witnessHasMoreWords = lastWord.position < witness.size();
    Word nextWord = witnessHasMoreWords ? witness.getWordOnPosition(lastWord.position + 1) : null;
    return nextWord;
  }

}
