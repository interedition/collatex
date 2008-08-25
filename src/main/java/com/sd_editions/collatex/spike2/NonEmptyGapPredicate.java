package com.sd_editions.collatex.spike2;

import com.google.common.base.Predicate;

public class NonEmptyGapPredicate implements Predicate<Gap> {

  public boolean apply(Gap gap) {
    return !gap.isEmpty();
  }

}
