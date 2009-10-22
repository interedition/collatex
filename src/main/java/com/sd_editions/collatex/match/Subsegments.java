package com.sd_editions.collatex.match;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.alignment.Phrase;
import eu.interedition.collatex.input.Segment;

public class Subsegments {
  private final Map<String, Subsegment> subsegments;
  private final Predicate<Subsegment> subsegmentIsOpen;
  private final Map<SegmentPosition, String> subsegmentTitlesAtSegmentPosition = Maps.newHashMap();

  public Subsegments() {
    subsegments = Maps.newHashMap();
    subsegmentIsOpen = new Predicate<Subsegment>() {
      @Override
      public boolean apply(Subsegment subsegment) {
        return subsegment.isOpen();
      }
    };
  }

  public Subsegment get(String title) {
    return subsegments.get(title);
  }

  public boolean containsTitle(String title) {
    return subsegments.containsKey(title);
  }

  public void add(String title, Subsegment subsegment) {
    subsegments.put(title, subsegment);
    for (SegmentPosition segmentPosition : subsegment.getSegmentPositions()) {
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
      return Iterables.find(all(), subsegmentIsOpen);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  public void close(String title) {
    Util.p("title to close", title);
    subsegments.get(title).close();
  }

  public String getSubsegmentTitleAtSegmentPosition(SegmentPosition next) {
    return subsegmentTitlesAtSegmentPosition.get(next);
  }

  public void join(String subsegmentTitle0, String subsegmentTitle1) {
    Subsegment subsegment = get(subsegmentTitle0);
    Subsegment nextSubsegment = get(subsegmentTitle1);
    Util.p("subsegmentTitle1", subsegmentTitle1);
    Util.p("subsegments", subsegments);
    subsegment.concat(nextSubsegment);
    add(subsegment.getTitle(), subsegment);
    removeSubsegment(subsegmentTitle0);
    removeSubsegment(subsegmentTitle1);
  }

  private void removeSubsegment(String subsegmentTitle) {
  //    subsegments.remove(subsegmentTitle);
  }

  @Override
  public String toString() {
    return subsegments.toString();
  }

  public List<Phrase> getPhrases(Segment segment) {
    List<Phrase> phrases = Lists.newArrayList();
    for (Subsegment subsegment : subsegments.values()) {
      Phrase phrase = subsegment.getPhrase(segment);
      if (phrase != null) phrases.add(phrase);
    }
    return phrases;
  }
}
