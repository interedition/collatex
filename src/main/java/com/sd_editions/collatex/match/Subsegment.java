package com.sd_editions.collatex.match;

import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.filter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class Subsegment {
  private String title;
  private boolean open = true;
  final Map<String, List<Integer>> startPositionsForPhrase = Maps.newHashMap();
  private int numberOfWords = 0;
  private boolean remove = false;

  public Subsegment(final String _title) {
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

  public void add(final String witnessId, final List<Integer> positions) {
    if (isClosed() || getNumberOfWords() > 1) {
      throw new RuntimeException("You're not allowed to add to a subsegment after it's been joined or closed.");
    }
    startPositionsForPhrase.put(witnessId, positions);
    numberOfWords = 1;
  }

  private int getNumberOfWords() {
    return numberOfWords;
  }

  public Set<Entry<String, List<Integer>>> entrySet() {
    return startPositionsForPhrase.entrySet();
  }

  public int size() {
    return startPositionsForPhrase.size();
  }

  public List<Integer> get(final String witnessId) {
    return startPositionsForPhrase.get(witnessId);
  }

  public void close() {
    open = false;
  }

  public Set<String> getWitnessIds() {
    return startPositionsForPhrase.keySet();
  }

  @SuppressWarnings("boxing")
  public List<SegmentPosition> getSegmentPositions() {
    final List<SegmentPosition> list = Lists.newArrayList();
    for (final Entry<String, List<Integer>> entry : startPositionsForPhrase.entrySet()) {
      final String witnessId = entry.getKey();
      for (final Integer position : entry.getValue()) {
        list.add(new SegmentPosition(witnessId, position + numberOfWords - 1));
      }
    }
    return list;
  }

  @Override
  public String toString() {
    return getTitle() + " " + startPositionsForPhrase.toString();
  }

  @SuppressWarnings("boxing")
  public Phrase getPhrase(final Segment segment) {
    Phrase phrase = null;
    final List<Integer> list = startPositionsForPhrase.get(segment.getWitnessId());
    if (list != null) {
      final int beginPosition = list.get(0);
      final Word beginWord = segment.getWordOnPosition(beginPosition);
      final int endPosition = beginPosition + numberOfWords - 1;
      final Word endWord = segment.getWordOnPosition(endPosition);
      phrase = new Phrase(segment, beginWord, endWord);
    }
    return phrase;
  }

  public void markForRemoval() {
    remove = true;
  }

  public boolean canRemove() {
    return remove;
  }

  private static final Predicate<List<Integer>> JUST_ONE_POSITION = new Predicate<List<Integer>>() {
    @Override
    public boolean apply(final List<Integer> positions) {
      return positions.size() == 1;
    }
  };

  public boolean isSingular() {
    // a subsegment is singular if for every witnessSegment involved in this subsegment, there is only 1 position
    return all(startPositionsForPhrase.values(), JUST_ONE_POSITION);
  }

  public Subsegment splitOff(final List<SegmentPosition> startPositions) {
    final Subsegment splitOff = new Subsegment(getTitle());
    final List<SegmentPosition> segmentPositions = getSegmentPositions();
    final Multimap<String, Integer> witnessPositions = Multimaps.newArrayListMultimap();
    for (final SegmentPosition segmentPosition : segmentPositions) {
      if (!startPositions.contains(segmentPosition)) {
        final String witnessId = segmentPosition.witnessId;
        final Integer position = segmentPosition.position;
        witnessPositions.put(witnessId, position);
        startPositionsForPhrase.get(witnessId).remove(position);
      }
    }
    for (final String witnessId : witnessPositions.keySet()) {
      splitOff.add(witnessId, (List<Integer>) witnessPositions.get(witnessId));
    }
    return splitOff;
  }

  public void concat(final Subsegment nextSubsegment) {
    title += " " + nextSubsegment.getTitle();
    numberOfWords += nextSubsegment.getNumberOfWords();
  }

  public void extend(final String nextWord) {
    title += " " + nextWord;
    numberOfWords += 1;
  }

  public void deleteSegmentPosition(final SegmentPosition segmentPosition) {
    startPositionsForPhrase.get(segmentPosition.witnessId).remove(segmentPosition.position);
    final Predicate<String> positionlist_is_empty = makePredicate(startPositionsForPhrase);
    final Iterable<String> witnesIdsToRemove = filter(startPositionsForPhrase.keySet(), positionlist_is_empty);
    for (final String witnessId : witnesIdsToRemove) {
      startPositionsForPhrase.remove(witnessId);
    }
    if (startPositionsForPhrase.isEmpty()) markForRemoval();
  }

  private Predicate<String> makePredicate(final Map<String, List<Integer>> _startPositionsForPhrase) {
    return new Predicate<String>() {
      @Override
      public boolean apply(final String witnessId) {
        return _startPositionsForPhrase.get(witnessId).isEmpty();
      }
    };
  }
}
