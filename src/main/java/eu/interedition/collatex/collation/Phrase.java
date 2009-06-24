package eu.interedition.collatex.collation;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class Phrase {
  private final Witness witness;
  private final int startPosition;
  private final int endPosition;
  private final int size;

  public Phrase(Witness _witness, int _size, int _startPosition, int _endPosition) {
    witness = _witness;
    this.size = _size;
    startPosition = _startPosition;
    endPosition = _endPosition;
  }

  public boolean hasGap() {
    return size > 0;
  }

  @Override
  public String toString() {
    List<String> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      String word = witness.getWordOnPosition(k).toString();
      words.add(word);
    }

    String replacementString = "";
    String divider = "";
    for (String replacement : words) {
      replacementString += divider + replacement;
      divider = " ";
    }
    return replacementString;
  }

  public Witness getWitness() {
    return witness;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public int getEndPosition() {
    return endPosition;
  }

  public List<Word> getWords() {
    List<Word> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      Word word = getWitness().getWordOnPosition(k);
      words.add(word);
    }
    return words;
  }

}
