package com.sd_editions.collatex.match_spike;

import java.util.List;

import com.google.common.collect.Lists;

public class WordMatches {
  private String word;
  private List<WordCoordinate> exactMatches = Lists.newArrayList();
  private List<WordCoordinate> levMatches = Lists.newArrayList();

  public WordMatches(String newWord) {
    this.word = newWord;
  }

  public String getWord() {
    return word;
  }

  public void addExactMatch(WordCoordinate matchCoordinate) {
    exactMatches.add(matchCoordinate);
  }

  public void addLevMatch(WordCoordinate matchCoordinate) {
    levMatches.add(matchCoordinate);
  }

  public List<WordCoordinate> getExactMatches() {
    return exactMatches;
  }

  public List<WordCoordinate> getLevMatches() {
    return levMatches;
  }

  @Override
  public String toString() {
    return word + ": exact=" + exactMatches + ", lev=" + levMatches;
  }

}
