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

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.match.worddistance.WordDistance;

public class Matches {
  private final Segment base;
  private final Segment witness;
  private List<Set<Match<Word>>> permutations;
  private final Set<Match> matches;
  private final WordDistance distanceMeasure;

  public Matches(final Segment _base, final Segment _witness, final WordDistance distanceMeasure) {
    this.base = _base;
    this.witness = _witness;
    this.distanceMeasure = distanceMeasure;
    this.matches = findMatches();
  }

  private Set<Match> findMatches() {
    final Set<Match> matchSet = Sets.newLinkedHashSet();
    for (final Word baseWord : base.getWords()) {
      for (final Word witnessWord : witness.getWords()) {
        if (baseWord._normalized.equals(witnessWord._normalized)) {
          matchSet.add(new Match(baseWord, witnessWord));
        } else {
          final float editDistance = distanceMeasure.distance(baseWord._normalized, witnessWord._normalized);
          if (editDistance < 0.5) matchSet.add(new Match(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }

  public List<Set<Match<Word>>> permutations() {
    if (permutations == null) permutations = new MatchPermutator(matches).permutations();
    return permutations;
  }

  public static <T extends BaseElement> List<Modification> getWordDistanceMatches(final Set<Match<T>> permutation) {
    final List<Modification> wordDistanceMatches = Lists.newArrayList();
    for (final Match<T> match : permutation) {
      if (match.wordDistance > 0) {
        wordDistanceMatches.add(new WordDistanceMatch(match));
      }
    }
    return wordDistanceMatches;
  }
}
