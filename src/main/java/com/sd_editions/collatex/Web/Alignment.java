package com.sd_editions.collatex.Web;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.spike2.Word;

public class Alignment {
  private final List<Word> words;
  public final int color;

  public Alignment(int _color) {
    this.words = Lists.newArrayList();
    this.color = _color;
  }

  public void add(Word word) {
    words.add(word);
  }

  public List<Word> getWords() {
    return words;
  }

}
