package com.sd_editions.collatex.permutations;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.match.worddistance.WordDistance;
import eu.interedition.collatex.visualization.Modification;

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
        if (baseWord.normalized.equals(witnessWord.normalized)) {
          matchSet.add(new Match(baseWord, witnessWord));
        } else {
          final float editDistance = distanceMeasure.distance(baseWord.normalized, witnessWord.normalized);
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
