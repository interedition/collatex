package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;

public class Comparison {

  private final List<Modification> modifications;

  public Comparison(Matches _matches) {
    modifications = Lists.newArrayList();
    List<Gap> gaps = _matches.getGaps();
    calculateModifications(gaps);
  }

  private void calculateModifications(List<Gap> gaps) {
    for (Gap gap : gaps) {
      if (gap.gapInBase() && gap.gapInWitness()) {
        modifications.add(new Replacement(gap.createBasePhrase(), gap.createWitnessPhrase()));
      } else if (gap.gapInBase() && !gap.gapInWitness()) {
        modifications.add(new Removal(gap.createBasePhrase()));
      } else if (!gap.gapInBase() && gap.gapInWitness()) {
        modifications.add(new Addition(gap.baseBeginPosition, gap.createWitnessPhrase()));
      }
    }
  }

  public List<Modification> getModifications() {
    return modifications;
  }

}
