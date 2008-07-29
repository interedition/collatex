package com.sd_editions.collatex.spike2;

import java.util.SortedSet;

import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;

public class Colors {

  private final Index index;
  private final TreeMultimap<Integer, Integer> colorsPerWitness;

  @SuppressWarnings("boxing")
  public Colors(String[] witnesses) {
    index = new Index(witnesses);
    //    System.out.println(colors);
    // pass two determine colors per witness
    colorsPerWitness = Multimaps.newTreeMultimap();
    int witnessIndex = 0;
    for (String witness : witnesses) {
      witnessIndex++;
      WitnessTokenizer tokenizer = new WitnessTokenizer(witness);
      while (tokenizer.hasNextToken()) {
        String token = tokenizer.nextToken();
        int color = index.getIndexof(token);
        colorsPerWitness.put(witnessIndex, color);
      }
    }

  }

  public int numberOfColors() {
    return index.numberOfEntries();
  }

  @SuppressWarnings("boxing")
  public SortedSet<Integer> getColorsPerWitness(int i) {
    return colorsPerWitness.get(i);
  }

  public Comparison compareWitness(int i, int j) {
    return new Comparison(getColorsPerWitness(i), getColorsPerWitness(j), index);
  }
}
