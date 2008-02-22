package com.sd_editions.collatex.Collate;

import java.util.Collections;
import java.util.List;

import com.sd_editions.collatex.Block.Word;

public class Replacement extends Cell {

  private final Word baseWord;
  private List<Word> replacementWords;

  public Replacement(Word baseWord, Word replacementWord) {
    this(baseWord, Collections.singletonList(replacementWord));
  }

  public Replacement(Word baseWord, List<Word> replacementWords) {
    this.baseWord = baseWord;
    this.replacementWords = replacementWords;
  }

  @Override
  public String toHTML() {
    return replacementsAsString();
  }

  @Override
  public String toString() {
    return "replacement: " + baseWord.getContent() + " / " + replacementsAsString();
  }

  private String replacementsAsString() {
    String replacementString = "";
    String divider = "";
    for (Word replacement : replacementWords) {
      replacementString += divider + replacement.getContent();
      divider = " ";
    }
    return replacementString;
  }

  @Override
  public String getType() {
    return "replacement";
  }

}
