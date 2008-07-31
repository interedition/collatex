package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;
import com.sd_editions.collatex.spike2.WitnessIndex;

public class Replacement extends Modification {

  private final WitnessIndex witnessIndex;
  private final WitnessIndex witnessIndex2;
  private final int i;
  private final int j;

  public Replacement(WitnessIndex _witnessIndex, int _i, WitnessIndex _witnessIndex2, int _j, int k) {
    this.witnessIndex = _witnessIndex;
    this.i = _i;
    this.witnessIndex2 = _witnessIndex2;
    this.j = _j;
  }

  @Override
  public String toString() {
    String baseWord = witnessIndex.getWordOnPosition(i); // TODO: must become a phrase!
    String replacementWord = witnessIndex2.getWordOnPosition(j); // TODO: muse become a phrase!
    return "replacement: " + baseWord + " / " + replacementWord + " position: " + i/*replacementsAsString()*/;
  }
  //  private String replacementsAsString() {
  //    String replacementString = "";
  //    String divider = "";
  //    for (Word replacement : replacementWords) {
  //      replacementString += divider + replacement.getContent();
  //      divider = " ";
  //    }
  //    return replacementString;
  //  }

}
