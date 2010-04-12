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

  public WordSegment(final String _title) {
    this.title = _title;
  }

  public void addWitnessPair(final String witnessId, final List<Word> words) {
    wordsPerWitness.put(witnessId, words);
    this.size = words.size();
  }

  @Override
  public String toString() {
    return title;
  }

  public void grow(final HashMap<String, Segment> witnessHash, final List<String> _wordsInSegments) {
    this.wordsInSegments = _wordsInSegments;
    boolean nextWordsMatch = true;
    while (nextWordsMatch) {
      final Set<String> nextWordSet = Sets.newHashSet();
      final Map<String, Word> nextWords = Maps.newHashMap();
      for (final List<Word> wordList : wordsPerWitness.values()) {
        final Word nextWord = getNextWord(wordList, witnessHash);
        if (nextWord == null || this.wordsInSegments.contains(OldSegmentExtractor.wordIdentifier(nextWord))) {
          nextWordSet.add(null);
        } else {
          nextWords.put(nextWord.getWitnessId(), nextWord);
          nextWordSet.add(nextWord._normalized);
        }
      }
      nextWordsMatch = (nextWordSet.size() == 1 && !nextWordSet.contains(null));
      if (nextWordsMatch) {
        for (final java.util.Map.Entry<String, List<Word>> entry : wordsPerWitness.entries()) {
          final Word nextWord = nextWords.get(entry.getKey());
          this.wordsInSegments.add(OldSegmentExtractor.wordIdentifier(nextWord));
          entry.getValue().add(nextWord);
        }
        this.title += " " + nextWordSet.iterator().next();
        this.size++;
      }
    }

  }

  private Word getNextWord(final List<Word> wordList, final HashMap<String, Segment> witnessHash) {
    final Word lastWord = wordList.get(wordList.size() - 1);
    final Segment witness = witnessHash.get(lastWord.getWitnessId());
    final boolean witnessHasMoreWords = lastWord.position < witness.wordSize();
    final Word nextWord = witnessHasMoreWords ? witness.getElementOnWordPosition(lastWord.position + 1) : null;
    return nextWord;
  }

  public int size() {
    return this.size;
  }

}
