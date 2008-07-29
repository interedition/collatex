package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Index {
  private final List<String> entries;

  public Index(String[] witnesses) {
    // pass one determine colors
    Set<String> words = Sets.newLinkedHashSet();
    for (String witness : witnesses) {
      WitnessTokenizer tokenizer = new WitnessTokenizer(witness);
      while (tokenizer.hasNextToken()) {
        String token = tokenizer.nextToken();
        words.add(token);
      }
    }
    entries = Lists.newArrayList(words);
  }

  public int numberOfEntries() {
    return entries.size();
  }

  public int getIndexof(String token) {
    return entries.indexOf(token) + 1;
  }

  public String getWord(int color) {
    return entries.get(color - 1);
  }
}
