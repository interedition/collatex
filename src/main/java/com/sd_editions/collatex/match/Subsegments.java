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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;

public class Subsegments {
  private final Map<String, Subsegment> subsegments;
  private final Predicate<Subsegment> subsegmentIsOpen;
  private final Map<SegmentPosition, String> subsegmentTitlesAtSegmentPosition = Maps.newHashMap();

  public Subsegments() {
    subsegments = Maps.newHashMap();
    subsegmentIsOpen = new Predicate<Subsegment>() {
      @Override
      public boolean apply(final Subsegment subsegment) {
        return !subsegment.canRemove() && subsegment.isOpen();
      }
    };
  }

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
    }
  }

  public Collection<Subsegment> all() {
    return subsegments.values();
  }

  public int size() {
    return subsegments.size();
  }

  public Subsegment getFirstOpenSubsegment() {
    try {
      return find(all(), subsegmentIsOpen);
    } catch (final NoSuchElementException e) {
      return null;
    }
  }

  public void close(final String title) {
    Util.p("title to close", title);
    subsegments.get(title).close();
  }

  public String getSubsegmentTitleAtSegmentPosition(final SegmentPosition next) {
    return subsegmentTitlesAtSegmentPosition.get(next);
  }

  public Subsegment join(final String subsegmentTitle0, final String subsegmentTitle1) {
    final Subsegment subsegment = get(subsegmentTitle0);
    final Subsegment nextSubsegment = get(subsegmentTitle1);
    Util.p("subsegmentTitle1", subsegmentTitle1);
    Util.p("subsegments", subsegments);
    subsegment.concat(nextSubsegment);
    add(subsegment.getTitle(), subsegment);
    markForRemoval(subsegmentTitle0);
    markForRemoval(subsegmentTitle1);
    return subsegment;
  }

  private void markForRemoval(final String subsegmentTitle) {
    subsegments.get(subsegmentTitle).markForRemoval();
  }

  @Override
  public String toString() {
    return subsegments.toString();
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
    final List<String> removableKeys = Lists.newArrayList(transform(filter(subsegments.values(), IS_REMOVABLE), EXTRACT_KEY));
    for (final String key : removableKeys) {
      subsegments.remove(key);
    }
  }
}
