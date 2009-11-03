package com.sd_editions.collatex.match;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class Subsegment {
  private String title;
  private boolean open = true;
  private final Map<String, List<Integer>> map = Maps.newHashMap();
  private int numberOfWords = 0;

  public Subsegment(String _title) {
    this.title = _title;
  }

  public String getTitle() {
    return title;
  }

  public boolean isOpen() {
    return open;
  }

  private boolean isClosed() {
    return !isOpen();
  }

  public void add(String witnessId, List<Integer> positions) {
    if (isClosed() || getNumberOfWords() > 1) {
      throw new RuntimeException("You're not allowed to add to a subsegment after it's been joined or closed.");
    }

    map.put(witnessId, positions);
    numberOfWords = 1;
  }

  private int getNumberOfWords() {
    return numberOfWords;
  }

  public Set<Entry<String, List<Integer>>> entrySet() {
    return map.entrySet();
  }

  public int size() {
    return map.size();
  }

  public List<Integer> get(String witnessId) {
    return map.get(witnessId);
  }

  public void close() {
    open = false;
  }

  public Set<String> getWitnessIds() {
    return map.keySet();
  }

  public List<SegmentPosition> getSegmentPositions() {
    List<SegmentPosition> list = Lists.newArrayList();
    for (Entry<String, List<Integer>> entry : map.entrySet()) {
      String witnessId = entry.getKey();
      for (Integer position : entry.getValue()) {
        list.add(new SegmentPosition(witnessId, position));
      }
    }
    return list;
  }

  public void concat(Subsegment nextSubsegment) {
    title += " " + nextSubsegment.getTitle();
    numberOfWords += nextSubsegment.getNumberOfWords();
  }

  @Override
  public String toString() {
    return getTitle() + " " + map.toString();
  }

  @SuppressWarnings("boxing")
  public Phrase getPhrase(Segment segment) {
    Phrase phrase = null;
    List<Integer> list = map.get(segment.getWitnessId());
    if (list != null) {
      int beginPosition = list.get(0);
      Word beginWord = segment.getWordOnPosition(beginPosition);
      int endPosition = beginPosition + numberOfWords - 1;
      Word endWord = segment.getWordOnPosition(endPosition);
      phrase = new Phrase(segment, beginWord, endWord);
    }
    return phrase;
  }
}
