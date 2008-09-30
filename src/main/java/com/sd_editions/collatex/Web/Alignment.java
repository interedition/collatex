package com.sd_editions.collatex.Web;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.spike2.Word;

public class Alignment {
  private final List<Word> words;

  public Alignment() {
    this.words = Lists.newArrayList();
  }

  public void add(Word word) {
    words.add(word);
  }

}
