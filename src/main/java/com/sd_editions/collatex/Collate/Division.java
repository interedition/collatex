package com.sd_editions.collatex.Collate;

import java.util.List;

import com.sd_editions.collatex.Block.Word;

public class Division extends Cell {

  Word base;
  List<Word> dividedWords;
  String dividedString = "";

  public Division(Word baseWord, List<Word> dividedWords1) {
    this.base = baseWord;
    this.dividedWords = dividedWords1;
    String divider = "";
    for (Word word : dividedWords) {
      dividedString += divider + word.getContent();
      divider = " ";
    }
  }

  @Override
  public String toString() {
    return "division: " + base.getContent() + " -> " + dividedString;
  }

  @Override
  public String getType() {
    return "division";
  }

  @Override
  public String toHTML() {
    return this.dividedString;
  }

}
