package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;
import com.sd_editions.collatex.spike2.WitnessIndex;

public class Addition extends Modification {
  private final int addedWord;
  private final int position;
  private final WitnessIndex witnessIndex2;

  @SuppressWarnings("boxing")
  public Addition(int _addedWord, WitnessIndex _witnessIndex2) {
    this.addedWord = _addedWord;
    this.witnessIndex2 = _witnessIndex2;
    this.position = witnessIndex2.getPosition(addedWord);
  }

  public int getAddedWord() {
    return addedWord;
  }

  public int getPosition() {
    return position;
  }

  @Override
  public String toString() {
    return "addition: " + witnessIndex2.getWordOnPosition(position) + " position: " + position;
  }
}
