package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;

public class Addition extends Modification {
  private final int addedWord;
  private final int position;

  public Addition(int _addedWord, int _position) {
    this.addedWord = _addedWord;
    this.position = _position;
  }

  public int getAddedWord() {
    return addedWord;
  }

  public int getPosition() {
    return position;
  }
}
