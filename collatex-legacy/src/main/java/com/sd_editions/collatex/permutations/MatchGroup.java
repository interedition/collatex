/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.permutations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Match;

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

  public void addAll(Iterable<Match> _matches) {
    matches.addAll(Lists.newArrayList(_matches));
  }

  public boolean isEmpty() {
    return matches.isEmpty();
  }

  public void remove(Match m) {
    matches.remove(m);
  }
}
