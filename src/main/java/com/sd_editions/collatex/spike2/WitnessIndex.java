package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
    Map<String, Integer> occurrences = Maps.newHashMap();
    while (tokenizer.hasNextToken()) {
      String token = tokenizer.nextToken();
      if (occurrences.get(token) == null)
        occurrences.put(token, 1);
      else
        occurrences.put(token, occurrences.get(token) + 1);
      int code = index.getIndexof(token, occurrences.get(token));
      wordCodes.add(code);
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

  public int getPosition(Integer word) {
    return getWordCodesList().indexOf(word);
  }

  @SuppressWarnings("boxing")
  public int getWordOnPosition(Integer position) {
    return getWordCodesList().get(position);
  }

  public List<Integer> getWordCodesList() {
    List<Integer> list = Lists.newArrayList(wordCodes);
    return list;
  }
}
