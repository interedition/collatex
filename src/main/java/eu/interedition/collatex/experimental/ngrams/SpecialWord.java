package eu.interedition.collatex.experimental.ngrams;

import eu.interedition.collatex.input.Word;

public class SpecialWord extends Word {

  public SpecialWord(final String witnessId, final String original, final int position) {
    super(witnessId, original, position);
  }

  @Override
  public String getNormalized() {
    return "#";
  }

}
