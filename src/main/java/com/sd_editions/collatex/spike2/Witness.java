package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;

public class Witness {
  private final List<Word> words;

  public Witness(String witness) {
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, false);
    words = Lists.newArrayList();
    int position = 1;
    while (tokenizer.hasNextToken()) {
      words.add(new Word(tokenizer.nextToken(), position));
      position++;
    }
  }

  public Witness(Word... _words) {
    this.words = Lists.newArrayList(_words);
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
}
