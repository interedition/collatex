package com.sd_editions.collatex.Collate;

import java.util.List;

import com.sd_editions.collatex.Block.Word;

public class Transposition extends Cell {

  private final Word transpositionWord;

  public Transposition(List<Word> transpositionWord) {
    this.transpositionWord = transpositionWord.get(0);
  }

  public Transposition(Word transpositionWord) {
    this.transpositionWord = transpositionWord;
  }

  @Override
  public String toString() {
    return "transposition: " + transpositionWord.getContent();
  }

  @Override
  public String toHTML() {
    return transpositionWord.getContent();
  }

  @Override
  public String getType() {
    return "transposition";
  }

}
