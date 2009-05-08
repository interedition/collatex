package com.sd_editions.collatex.permutations;

import com.google.common.base.Predicate;

public class ValidNonMatchPredicate implements Predicate<NonMatch> {

  public boolean apply(NonMatch nonMatch) {
    return nonMatch.isValid();
  }
}
