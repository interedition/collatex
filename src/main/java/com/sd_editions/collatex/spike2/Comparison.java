package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Comparison {

  private final SortedSet<Integer> colorsPerWitness;
  private final SortedSet<Integer> colorsPerWitness2;
  private final List<String> colors;

  public Comparison(@SuppressWarnings("hiding") SortedSet<Integer> colorsPerWitness, @SuppressWarnings("hiding") SortedSet<Integer> colorsPerWitness2, @SuppressWarnings("hiding") List<String> colors) {
    this.colorsPerWitness = colorsPerWitness;
    this.colorsPerWitness2 = colorsPerWitness2;
    this.colors = colors;
  }

  @SuppressWarnings("boxing")
  public List<String> getAddedWords() {
    Set<Integer> result = Sets.newLinkedHashSet(colorsPerWitness2);
    result.removeAll(colorsPerWitness);
    List<String> additions = Lists.newArrayList();
    for (Integer color : result) {
      additions.add(colors.get(color - 1));
    }
    return additions;
  }
}
