package com.sd_editions.collatex.permutations;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.match.worddistance.WordDistance;

public class Matches {
  private final Witness base;
  private final Witness witness;
  private List<Set<Match>> permutations;
  private final Set<Match> matches;
  private final WordDistance distanceMeasure;

  public Matches(Witness _base, Witness _witness, WordDistance distanceMeasure) {
    this.base = _base;
    this.witness = _witness;
    this.distanceMeasure = distanceMeasure;
    this.matches = findMatches();
  }

  private Set<Match> findMatches() {
    Set<Match> matchSet = Sets.newLinkedHashSet();
    for (Word baseWord : base.getWords()) {
      for (Word witnessWord : witness.getWords()) {
        if (baseWord.normalized.equals(witnessWord.normalized)) {
          matchSet.add(new Match(baseWord, witnessWord));
        } else {
          float editDistance = distanceMeasure.distance(baseWord.normalized, witnessWord.normalized);
          if (editDistance < 0.5) matchSet.add(new Match(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }

  public List<Set<Match>> permutations() {
    if (permutations == null) permutations = new MatchPermutator(matches).permutations();
    return permutations;
  }

  public static List<Modification> getLevenshteinMatches(Set<Match> permutation) {
    List<Modification> levenshtein = Lists.newArrayList();
    for (Match match : permutation) {
      if (match.levenshteinDistance > 0) {
        levenshtein.add(new LevenshteinMatch(match));
      }
    }
    return levenshtein;
  }
}
