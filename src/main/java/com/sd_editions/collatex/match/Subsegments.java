package com.sd_editions.collatex.match;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;

public class Subsegments {
  private final Map<String, Subsegment> subsegments = Maps.newHashMap();
  private final Map<SegmentPosition, String> subsegmentTitlesAtSegmentPosition = Maps.newHashMap();

  public Subsegment get(final String title) {
    return subsegments.get(title);
  }

  public boolean containsTitle(final String title) {
    return subsegments.containsKey(title);
  }

  public void add(final String title, final Subsegment subsegment) {
    subsegments.put(title, subsegment);
    for (final SegmentPosition segmentPosition : subsegment.getSegmentPositions()) {
      subsegmentTitlesAtSegmentPosition.put(segmentPosition, title);
      SegmentPosition nextSegmentPosition = segmentPosition.nextSegmentPosition();
      for (int i = 2; i < subsegment.getNumberOfWords(); i++) {
        subsegmentTitlesAtSegmentPosition.put(nextSegmentPosition, title);
        nextSegmentPosition = nextSegmentPosition.nextSegmentPosition();
      }
    }
  }

  public Collection<Subsegment> all() {
    return subsegments.values();
  }

  public int size() {
    return subsegments.size();
  }

  private static final Predicate<Subsegment> SUBSEGMENT_IS_OPEN = new Predicate<Subsegment>() {
    @Override
    public boolean apply(final Subsegment subsegment) {
      return !subsegment.canRemove() && subsegment.isOpen();
    }
  };

  public Subsegment getFirstOpenSubsegment() {
    try {
      return find(all(), SUBSEGMENT_IS_OPEN);
    } catch (final NoSuchElementException e) {
      return null;
    }
  }

  public void close(final String title) {
    //    Util.p("title to close", title);
    subsegments.get(title).close();
  }

  public String getSubsegmentTitleAtSegmentPosition(final SegmentPosition next) {
    return subsegmentTitlesAtSegmentPosition.get(next);
  }

  public Subsegment join(final String subsegmentTitle0, final String subsegmentTitle1/*, final List<SegmentPosition> startPositions*/) {
    final Subsegment subsegment = get(subsegmentTitle0);
    //    if (!subsegment.isSingular()) {
    //      final Subsegment splitOff = subsegment.splitOff(startPositions);
    //      add(splitOff.getTitle(), splitOff);
    //    }

    final Subsegment nextSubsegment = get(subsegmentTitle1);
    //    Util.p("subsegmentTitle0", subsegmentTitle0);
    //    Util.p("subsegmentTitle1", subsegmentTitle1);
    //    Util.p("subsegments", subsegments);
    subsegment.concat(nextSubsegment);
    add(subsegment.getTitle(), subsegment);
    markForRemoval(subsegmentTitle0);
    markForRemoval(subsegmentTitle1);
    //    Util.p("subsegment", subsegment);
    //    for (final SegmentPosition segmentPosition : startPositions) {
    //      subsegmentTitlesAtSegmentPosition.put(segmentPosition, subsegment.getTitle());
    //    }
    return subsegment;
  }

  private void markForRemoval(final String subsegmentTitle) {
    subsegments.get(subsegmentTitle).markForRemoval();
  }

  @Override
  public String toString() {
    final List<String> titleList = Lists.newArrayList(subsegments.keySet());
    Collections.sort(titleList);
    final List<String> items = Lists.newArrayList();
    for (final String title : titleList) {
      items.add("'" + title + "'=" + subsegments.get(title).toString());
    }
    return Join.join("\n", items);
  }

  static final Comparator<Phrase> SORT_ON_STARTPOSITION = new Comparator<Phrase>() {
    @Override
    public int compare(final Phrase p1, final Phrase p2) {
      return p1.getStartPosition() - p2.getStartPosition();
    }
  };

  public List<Phrase> getPhrases(final Segment segment) {
    final List<Phrase> phrases = Lists.newArrayList();
    for (final Subsegment subsegment : subsegments.values()) {
      final Phrase phrase = subsegment.getPhrase(segment);
      if (phrase != null) phrases.add(phrase);
    }
    Collections.sort(phrases, SORT_ON_STARTPOSITION);
    return phrases;
  }

  final Predicate<Subsegment> IS_REMOVABLE = new Predicate<Subsegment>() {
    public boolean apply(final Subsegment s) {
      return s.canRemove();
    }
  };
  final Function<Subsegment, String> EXTRACT_KEY = new Function<Subsegment, String>() {
    @Override
    public String apply(final Subsegment s) {
      return s.getTitle();
    }
  };

  public void removeMarkedSubsegments() {
    for (final String key : Lists.newArrayList(transform(filter(subsegments.values(), IS_REMOVABLE), EXTRACT_KEY))) {
      subsegments.remove(key);
    }
  }

  public void reindex() {
    subsegmentTitlesAtSegmentPosition.clear();
    //  private Map<SegmentPosition, String> getSequencesAtSegmentPosition(final Subsegments _subsegments) {
    for (final Subsegment subsegment : all()) {
      final String sequenceTitle = subsegment.getTitle();
      for (final Entry<String, List<Integer>> positionsPerWitnessEntry : subsegment.entrySet()) {
        final String witnessId = positionsPerWitnessEntry.getKey();
        final List<Integer> positions = positionsPerWitnessEntry.getValue();
        for (final Integer position : positions) {
          subsegmentTitlesAtSegmentPosition.put(new SegmentPosition(witnessId, position), sequenceTitle);
        }
      }
      //      Util.p(sequenceTitle + " occurs in " + subsegment.size() + " witnesses.");
      //      Util.p(sequencesAtSegmentPosition);
    }
    //    return sequencesAtSegmentPosition;
    //  }
    //    Util.p("subsegmentTitlesAtSegmentPosition", subsegmentTitlesAtSegmentPosition);
  }
}
