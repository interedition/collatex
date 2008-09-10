package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

@SuppressWarnings("boxing")
public class WitnessIndex {
  private final Set<Integer> wordCodes;
  private final List<String> words;
  private final Index index;
  private final Map<Integer, Integer> expectations;

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
    this.expectations = calculateSequenceExpectations();
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

  //TODO: remove!
  public String getWordOnPosition(int position) {
    return words.get(position - 1);
  }

  //TODO: after remove, rename!
  public Word getNewWordOnPosition(int position) {
    return new Word(words.get(position - 1), position);
  }

  public int size() {
    return wordCodes.size();
  }

  public Phrase createPhrase(int i, int j) {
    return new Phrase(this, i, j);
  }

  // step 1 take the matches
  // step 2 walk over the witness index and filter away everything that is not a match

  public List<Integer> sortMatchesByPosition(final Set<Integer> matches) {
    List<Integer> wordCodesList = getWordCodesList();
    List<Integer> onlyMatches = Lists.newArrayList(Iterables.filter(wordCodesList, new Predicate<Integer>() {
      public boolean apply(Integer wordCode) {
        return matches.contains(wordCode);
      }
    }));
    return onlyMatches;
  }

  public List<Integer> getPositionsOfMatchesInSequence(Set<Integer> matches) {
    List<Integer> matchPositions = Lists.newArrayList();
    for (Integer match : matches) {
      matchPositions.add(getPosition(match));
    }
    Collections.sort(matchPositions);
    return matchPositions;
  }

  public List<Gap> getGaps(Set<Integer> matches) {
    int currentIndex = 1;
    List<Integer> positions = getPositionsOfMatchesInSequence(matches);
    List<Gap> gaps = Lists.newArrayList();
    for (Integer position : positions) {
      int indexDif = position - currentIndex;
      gaps.add(new Gap(this, indexDif, currentIndex, position - 1));
      currentIndex = 1 + position;
    }
    int IndexDif = size() - currentIndex + 1;
    gaps.add(new Gap(this, IndexDif, currentIndex, size()));
    return gaps;
  }

  private Map<Integer, Integer> calculateSequenceExpectations() {
    List<Integer> _wordCodes = getWordCodesList();
    Map<Integer, Integer> _expectations = Maps.newHashMap();
    if (wordCodes.isEmpty()) {
      return _expectations;
    }
    Iterator<Integer> i = _wordCodes.iterator();
    Integer previous = i.next();
    while (i.hasNext()) {
      Integer next = i.next();
      _expectations.put(next, previous);
      previous = next;
    }
    return _expectations;
  }

  public Integer getPreviousWordCode(Integer wordCode) {
    return expectations.get(wordCode);
  }

}
