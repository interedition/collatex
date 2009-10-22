package eu.interedition.collatex.matching;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class Subsegments {
  private final Map<String, Subsegment> subsegments;
  private final Predicate<Subsegment> subsegmentIsOpen;

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
    subsegments.get(title).close();
  }
}
