package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;

public class Comparison {

  private final List<Modification> modifications;
  private final WitnessIndex witnessIndex;
  private final WitnessIndex witnessIndex2;
  private final Matches matches;

  public Comparison(WitnessIndex _witnessIndex, WitnessIndex _witnessIndex2) {
    this.witnessIndex = _witnessIndex;
    this.witnessIndex2 = _witnessIndex2;
    modifications = Lists.newArrayList();
    matches = new Matches(witnessIndex, witnessIndex2);
    List<Gap> gaps = getMatches().getGaps();
    calculateModifications(gaps);
  }

  private void calculateModifications(List<Gap> gaps) {
    for (Gap gap : gaps) {
      if (gap.distanceBase > 0 && gap.distanceWitness > 0) {
        Phrase original = witnessIndex.createPhrase(gap.baseBeginPosition, gap.baseEndPosition);
        Phrase replacement = witnessIndex2.createPhrase(gap.witnessBeginPosition, gap.witnessEndPosition);
        modifications.add(new Replacement(original, replacement));
      } else if (gap.distanceBase > 0 && gap.distanceWitness == 0) {
        modifications.add(new Removal(witnessIndex.createPhrase(gap.baseBeginPosition, gap.baseEndPosition)));
      } else if (gap.distanceBase == 0 && gap.distanceWitness > 0) {
        Phrase addition = witnessIndex2.createPhrase(gap.witnessBeginPosition, gap.witnessEndPosition);
        modifications.add(new Addition(gap.baseBeginPosition, addition));
      }
    }
  }

  public List<Modification> getModifications() {
    return modifications;
  }

  public Matches getMatches() {
    return matches;
  }

}
