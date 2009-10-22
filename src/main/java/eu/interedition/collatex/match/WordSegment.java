package eu.interedition.collatex.match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class WordSegment {

  public String title;
  private final Multimap<String, List<Word>> wordsPerWitness = Multimaps.newArrayListMultimap();
  private int size;
  public List<String> wordsInSegments;

  public WordSegment(String _title) {
    this.title = _title;
  }

  public void addWitnessPair(String witnessId, List<Word> words) {
    wordsPerWitness.put(witnessId, words);
    this.size = words.size();
  }

  @Override
  public String toString() {
    return title;
  }

  public void grow(HashMap<String, Segment> witnessHash, List<String> _wordsInSegments) {
    this.wordsInSegments = _wordsInSegments;
    boolean nextWordsMatch = true;
    while (nextWordsMatch) {
      Set<String> nextWordSet = Sets.newHashSet();
      Map<String, Word> nextWords = Maps.newHashMap();
      for (List<Word> wordList : wordsPerWitness.values()) {
        Word nextWord = getNextWord(wordList, witnessHash);
        if (nextWord == null || this.wordsInSegments.contains(SegmentExtractor.wordIdentifier(nextWord))) {
          nextWordSet.add(null);
        } else {
          nextWords.put(nextWord.getWitnessId(), nextWord);
          nextWordSet.add(nextWord.normalized);
        }
      }
      nextWordsMatch = (nextWordSet.size() == 1 && !nextWordSet.contains(null));
      if (nextWordsMatch) {
        for (java.util.Map.Entry<String, List<Word>> entry : wordsPerWitness.entries()) {
          Word nextWord = nextWords.get(entry.getKey());
          this.wordsInSegments.add(SegmentExtractor.wordIdentifier(nextWord));
          entry.getValue().add(nextWord);
        }
        this.title += " " + nextWordSet.iterator().next();
        this.size++;
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

  public int size() {
    return this.size;
  }

}
