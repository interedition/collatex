package com.sd_editions.collatex.permutations;

import com.google.common.base.Predicate;

public class ValidMismatchPredicate implements Predicate<MisMatch> {

  public boolean apply(MisMatch mismatch) {
    return mismatch.isValid();
  }
}
