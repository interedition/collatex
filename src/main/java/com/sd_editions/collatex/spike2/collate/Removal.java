package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;

public class Removal extends Modification {
  private final int removedWord;
  private final int position;

  public Removal(int _removedWord, int _position) {
    this.removedWord = _removedWord;
    this.position = _position;
  }

  public int getAddedWord() {
    return removedWord;
  }

  public int getPosition() {
    return position;
  }
}
