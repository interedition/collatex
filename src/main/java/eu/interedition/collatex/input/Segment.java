package eu.interedition.collatex.input;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Util;
import eu.interedition.collatex.input.visitors.ICollationResource;
import eu.interedition.collatex.input.visitors.IResourceVisitor;

public class Segment implements ICollationResource {
  public final String id;
  private final List<Word> words;

  public Segment(Word... _words) {
    if (_words == null) throw new IllegalArgumentException("List of words cannot be null.");
    if (_words.length == 0)
      this.id = Util.generateRandomId();
    else
      this.id = _words[0].getWitnessId();
    this.words = Lists.newArrayList(_words);
  }

  public Segment(String _id, List<Word> _words) {
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

  public void accept(IResourceVisitor visitor) {
    visitor.visitWitness(this);
    List<Word> words2 = getWords();
    for (Word word : words2) {
      visitor.visitWord(word);
    }
    visitor.postVisitWitness(this);
  }
}
