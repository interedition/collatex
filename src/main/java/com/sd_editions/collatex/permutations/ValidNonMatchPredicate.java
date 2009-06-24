package com.sd_editions.collatex.permutations;

import com.google.common.base.Predicate;

import eu.interedition.collatex.collation.gaps.Gap;

public class ValidNonMatchPredicate implements Predicate<Gap> {

  public boolean apply(Gap nonMatch) {
    return nonMatch.isValid();
  }
}
