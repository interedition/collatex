package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class WitnessIndex {
  private final Set<Integer> wordCodes;
  private final List<String> words;
  private final Index index;

  @SuppressWarnings("boxing")
  public WitnessIndex(String witness, Index _index) {
    index = _index;

    WitnessTokenizer tokenizer = index.createNormalizedTokenizerFor(witness);
    wordCodes = Sets.newLinkedHashSet();
    while (tokenizer.hasNextToken()) {
      String token = tokenizer.nextToken();
      int color = index.getIndexof(token);
      wordCodes.add(color);
    }

    tokenizer = index.createTokenizerFor(witness);
    words = Lists.newArrayList();
    while (tokenizer.hasNextToken()) {
      words.add(tokenizer.nextToken());
    }
  }

  public Set<Integer> getWordCodes() {
    return wordCodes;
  }

  public List<String> getWords() {
    return words;
  }

  public Index getIndex() {
    return index;
  }
}
