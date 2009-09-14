package eu.interedition.collatex.input;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

public class Witness {
  public final String id;
  private final List<Word> words;

  public Witness(Word... _words) {
    if (_words == null) throw new IllegalArgumentException("List of words cannot be null.");
    if (_words.length == 0)
      this.id = Long.toString(Math.abs(new Random().nextLong()), 5);
    else
      this.id = _words[0].getWitnessId();
    this.words = Lists.newArrayList(_words);
  }

  public Witness(String _id, List<Word> _words) {
    this.id = _id;
    this.words = _words;
  }

  public List<Word> getWords() {
    return words;
  }

  public Word getWordOnPosition(int position) {
    return words.get(position - 1);
  }

  public int size() {
    return words.size();
  }

  // Note: part copied from Phrase
  @Override
  public String toString() {
    String replacementString = "";
    String divider = "";
    for (Word word : words) {
      replacementString += divider + word;
      divider = " ";
    }
    return replacementString;
  }
}
