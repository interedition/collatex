package com.sd_editions.collatex.permutations;

import com.google.common.base.Predicate;

import eu.interedition.collatex.alignment.Gap;

public class ValidNonMatchPredicate implements Predicate<Gap> {

  public boolean apply(Gap nonMatch) {
    return nonMatch.isValid();
  }
}
