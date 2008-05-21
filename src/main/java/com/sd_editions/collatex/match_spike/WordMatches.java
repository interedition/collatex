package com.sd_editions.collatex.match_spike;

import java.util.ArrayList;
import java.util.List;

public class WordMatches {
  private String word;
  private List<WordCoordinate> exactMatches = new ArrayList<WordCoordinate>();
  private List<WordCoordinate> levMatches = new ArrayList<WordCoordinate>();

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

}
