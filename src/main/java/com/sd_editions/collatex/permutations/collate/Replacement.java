package com.sd_editions.collatex.permutations.collate;

import com.sd_editions.collatex.permutations.Modification;
import com.sd_editions.collatex.permutations.Phrase;

public class Replacement extends Modification {
  private final Phrase original;
  private final Phrase replacement;

  public Replacement(Phrase _original, Phrase _replacement) {
    this.original = _original;
    this.replacement = _replacement;
  }

  @Override
  public String toString() {
    String baseWords = original.toString();
    String replacementWords = replacement.toString();
    return "replacement: " + baseWords + " / " + replacementWords + " position: " + original.getStartPosition();
  }

  public int getPosition() {
    return original.getStartPosition();
  }

  public String getOriginalWords() {
    return original.toString();
  }

  public String getReplacementWords() {
    return replacement.toString();
  }

}
