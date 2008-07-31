package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

@SuppressWarnings("boxing")
public class WitnessIndex {
  private final Set<Integer> wordCodes;
  private final List<String> words;
  private final Index index;

  public WitnessIndex(String witness, Index _index) {
    index = _index;

    WitnessTokenizer tokenizer = index.createNormalizedTokenizerFor(witness);
    wordCodes = Sets.newLinkedHashSet();
    Multiset<String> occurrences = Multisets.newHashMultiset();
    while (tokenizer.hasNextToken()) {
      String token = tokenizer.nextToken();
      occurrences.add(token);
      int code = index.getCodeFor(token, occurrences.count(token));
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

  public int getPosition(Integer wordCode) {
    return getWordCodesList().indexOf(wordCode) + 1;
  }

  public int getWordCodeOnPosition(Integer position) {
    return getWordCodesList().get(position - 1);
  }

  public List<Integer> getWordCodesList() {
    List<Integer> list = Lists.newArrayList(wordCodes);
    return list;
  }

  public String getWordOnPosition(int position) {
    return words.get(position - 1);
  }

  public int size() {
    return wordCodes.size();
  }

  public Phrase createPhrase(int i, int j) {
    return new Phrase(this, i, j);
  }
}
