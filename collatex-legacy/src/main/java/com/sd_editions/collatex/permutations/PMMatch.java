package com.sd_editions.collatex.permutations;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import eu.interedition.collatex.input.Word;

/* Possible MultiMatch */
public class PMMatch {
  final MultiMatch match;
  private boolean fixed;

  public PMMatch(MultiMatch _match) {
    match = _match;
    fixed = false;
  }

  public void fix() {
    this.fixed = true;
  }

  public boolean isFixed() {
    return fixed;
  }

  @Override
  public String toString() {
    Function<Word, Integer> extractPosition = new Function<Word, Integer>() {
      @SuppressWarnings("boxing")
      @Override
      public Integer apply(Word word) {
        return word.position;
      }
    };
    // words.collect{|w| w.position}
    return "[" + Joiner.on("->").join(Iterables.transform(getWords(), extractPosition)) + "]" + (this.isFixed() ? " (fixed)" : " (potential)");
  }

  public List<Word> getWords() {
    return match.getWords();
  }

  public PMMatch copy() {
    PMMatch copy = new PMMatch(this.match);
    if (this.isFixed()) copy.fix();
    return copy;
  }
}
