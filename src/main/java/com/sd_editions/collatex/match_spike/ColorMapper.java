package com.sd_editions.collatex.match_spike;

import java.util.HashMap;

import com.google.common.collect.Maps;

public class ColorMapper {

  private final WordMatchMap wordMatchMap;
  private final HashMap<String, Integer> colorMap;
  private int color;

  public ColorMapper(WordMatchMap wordMatchMap1) {
    wordMatchMap = wordMatchMap1;
    colorMap = Maps.newHashMap();
    color = 1;
  }

  public Integer determineColor(String normalizedWord, int witness_index, int word_index) {
    if (!colorMap.containsKey(normalizedWord)) colorMap.put(normalizedWord, new Integer(color++));
    return colorMap.get(normalizedWord);
  }

}
