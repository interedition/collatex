package com.sd_editions.collatex.output;

import com.sd_editions.collatex.permutations.Word;

public class MatchColumn extends Column {

  private final Word matchedWord;

  public MatchColumn(Word _matchedWord) {
    this.matchedWord = _matchedWord;
  }

  @Override
  public void toXML(StringBuilder builder) {
    builder.append(matchedWord.original);
  }

}
