package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;
import com.sd_editions.collatex.spike2.WitnessIndex;

public class Removal extends Modification {
  private final WitnessIndex witnessIndex;
  private final int start_position;
  private final int end_position;

  //TODO: should become phrase!
  public Removal(WitnessIndex _witnessIndex, int _start_position, int _end_position) {
    this.witnessIndex = _witnessIndex;
    this.start_position = _start_position;
    this.end_position = _end_position;
  }

  @SuppressWarnings("boxing")
  public int getAddedWord() {
    return witnessIndex.getWordCodeOnPosition(start_position);
  }

  public int getPosition() {
    return start_position;
  }

  @Override
  public String toString() {
    return "omission: " + witnessIndex.getWordOnPosition(start_position);
  }
}
