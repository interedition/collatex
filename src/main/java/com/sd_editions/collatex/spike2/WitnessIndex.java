package com.sd_editions.collatex.spike2;

import java.util.Set;

import com.google.common.collect.Sets;

public class WitnessIndex {
  private final Set<Integer> words;
  private final Index index;

  @SuppressWarnings("boxing")
  public WitnessIndex(String witness, Index _index) {
    index = _index;
    WitnessTokenizer tokenizer = index.createTokenizerFor(witness);
    words = Sets.newLinkedHashSet();
    while (tokenizer.hasNextToken()) {
      String token = tokenizer.nextToken();
      int color = index.getIndexof(token);
      words.add(color);
    }

  }

  public Set<Integer> getWords() {
    return words;
  }

  public Index getIndex() {
    return index;
  }
}
