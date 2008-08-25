package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;

public class Comparison {

  private final List<Modification> modifications;

  public Comparison(Matches _matches) {
    modifications = Lists.newArrayList();
    List<Gap> gaps = _matches.getGaps();
    for (Gap gap : gaps) {
      modifications.add(gap.analyse());
    }
  }

  public List<Modification> getModifications() {
    return modifications;
  }

}
