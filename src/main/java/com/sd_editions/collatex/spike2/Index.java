package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Index {
  private final List<String> normalizedEntries;

  public Index(String[] witnesses) {
    // pass one determine colors
    Set<String> words = Sets.newLinkedHashSet();
    for (String witness : witnesses) {
      WitnessTokenizer tokenizer = createNormalizedTokenizerFor(witness);
      while (tokenizer.hasNextToken()) {
        String token = tokenizer.nextToken();
        words.add(token);
      }
    }
    normalizedEntries = Lists.newArrayList(words);
  }

  public int numberOfEntries() {
    return normalizedEntries.size();
  }

  public int getIndexof(String word) {
    return normalizedEntries.indexOf(word) + 1;
  }

  public int getCodeFor(String word, int occurrence) {
    return (occurrence - 1) * normalizedEntries.size() + getIndexof(word);
  }

  public String getWord(int index) {
    return normalizedEntries.get(index - 1);
  }

  public WitnessTokenizer createNormalizedTokenizerFor(String witness) {
    return new WitnessTokenizer(witness, true);
  }

  public WitnessTokenizer createTokenizerFor(String witness) {
    return new WitnessTokenizer(witness, false);
  }
}
