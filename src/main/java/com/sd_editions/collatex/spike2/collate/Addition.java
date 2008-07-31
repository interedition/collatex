package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;
import com.sd_editions.collatex.spike2.WitnessIndex;

public class Addition extends Modification {
  private final int position;
  private final WitnessIndex witnessIndex2;

  public Addition(int _position, WitnessIndex _witnessIndex2) {
    this.witnessIndex2 = _witnessIndex2;
    this.position = _position;
  }

  @SuppressWarnings("boxing")
  public int getAddedWord() {
    return witnessIndex2.getWordCodeOnPosition(position);
  }

  public int getPosition() {
    return position;
  }

  @Override
  public String toString() {
    return "addition: " + witnessIndex2.getWordOnPosition(position) + " position: " + position;
  }
}
