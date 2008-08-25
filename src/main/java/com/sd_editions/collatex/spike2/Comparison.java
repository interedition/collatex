package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;

public class Comparison {

  private final List<Modification> modifications;

  public Comparison(Matches _matches) {
    modifications = Lists.newArrayList();
    List<MisMatch> mismatches = _matches.getMismatches();
    for (MisMatch mismatch : mismatches) {
      modifications.add(mismatch.analyse());
    }
  }

  public List<Modification> getModifications() {
    return modifications;
  }

}
