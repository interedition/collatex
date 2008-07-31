package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;

public class Phrase {
  private final WitnessIndex witnessIndex;
  private final int start_position;
  private final int end_position;

  public Phrase(WitnessIndex _witnessIndex, int _start_position, int _end_position) {
    witnessIndex = _witnessIndex;
    start_position = _start_position;
    end_position = _end_position;
  }

  @Override
  public String toString() {
    List<String> words = Lists.newArrayList();
    for (int k = start_position; k <= end_position; k++) {
      String word = witnessIndex.getWordOnPosition(k);
      words.add(word);
    }

    String replacementString = "";
    String divider = "";
    for (String replacement : words) {
      replacementString += divider + replacement;
      divider = " ";
    }
    return replacementString;
  }

}
