package com.sd_editions.collatex.permutations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.collation.Match;

public class MatchGroup implements Iterable<Match> {

  private final ArrayList<Match> matches;

  public MatchGroup(Match... match) {
    this.matches = Lists.newArrayList(match);
  }

  public MatchGroup(Iterable<Match> _matches) {
    this.matches = Lists.newArrayList(_matches);
  }

  public Set<Match> asSet() {
    return Sets.newLinkedHashSet(matches);
  }

  public void sort(Comparator<Match> comparator) {
    Collections.sort(matches, comparator);
  }

  public int size() {
    return matches.size();
  }

  public Iterator<Match> iterator() {
    return matches.iterator();
  }

  public Match get(int i) {
    return matches.get(i);
  }

  public void add(Match m) {
    matches.add(m);
  }

  @Override
  public int hashCode() {
    return matches.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MatchGroup)) return false;
    MatchGroup matchgroup = (MatchGroup) obj;
    return matchgroup.matches.equals(matches);
  }

  @Override
  public String toString() {
    return matches.toString();
  }
}
