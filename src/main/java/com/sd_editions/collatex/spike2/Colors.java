package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

public class Colors {

  private final List<String> colors; // position in list determines color
  private final TreeMultimap<Integer, Integer> colorsPerWitness;

  @SuppressWarnings("boxing")
  public Colors(String[] witnesses) {
    // pass one determine colors
    Set<String> words = Sets.newLinkedHashSet();
    for (String witness : witnesses) {
      WitnessTokenizer tokenizer = new WitnessTokenizer(witness);
      while (tokenizer.hasNextToken()) {
        String token = tokenizer.nextToken();
        words.add(token);
      }
    }
    colors = Lists.newArrayList(words);
    //    System.out.println(colors);
    // pass two determine colors per witness
    colorsPerWitness = Multimaps.newTreeMultimap();
    int witnessIndex = 0;
    for (String witness : witnesses) {
      witnessIndex++;
      WitnessTokenizer tokenizer = new WitnessTokenizer(witness);
      while (tokenizer.hasNextToken()) {
        String token = tokenizer.nextToken();
        int color = colors.indexOf(token) + 1;
        colorsPerWitness.put(witnessIndex, color);
      }
    }

  }

  public int numberOfColors() {
    return colors.size();
  }

  @SuppressWarnings("boxing")
  public SortedSet<Integer> getColorsPerWitness(int i) {
    return colorsPerWitness.get(i);
  }
}
