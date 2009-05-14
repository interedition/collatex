package com.sd_editions.collatex.output;

import com.sd_editions.collatex.permutations.Witness;
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

  @Override
  public Word getWord(Witness witness) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addMatch(Witness witness, Word word) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsWitness(Witness witness) {
    throw new UnsupportedOperationException();
  }

}
