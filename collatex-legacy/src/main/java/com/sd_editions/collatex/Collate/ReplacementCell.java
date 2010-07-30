package com.sd_editions.collatex.Collate;

import java.util.Collections;
import java.util.List;

import com.sd_editions.collatex.Block.Word;

public class ReplacementCell extends Cell {

  private final Word baseWord;
  private final List<Word> replacementWords;

  public ReplacementCell(Word baseWord, Word replacementWord) {
    this(baseWord, Collections.singletonList(replacementWord));
  }

  public ReplacementCell(Word baseWord, List<Word> replacementWords) {
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
    StringBuilder replacementString = new StringBuilder();
    String divider = "";
    for (Word replacement : replacementWords) {
      replacementString.append(divider).append(replacement.getContent());
      divider = " ";
    }
    return replacementString.toString();
  }

  @Override
  public String getType() {
    return "replacement";
  }

}
