package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.base.Join;
import com.google.common.collect.Lists;

public class Witness {
  public final String sentence;
  private final List<Word> words;

  public Witness(String witness) {
    this.sentence = witness;
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, false);
    this.words = Lists.newArrayList();
    int position = 1;
    while (tokenizer.hasNextToken()) {
      this.words.add(new Word(tokenizer.nextToken(), position));
      position++;
    }
  }

  public Witness(Word... _words) {
    this.sentence = Join.join(" ", _words);
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
