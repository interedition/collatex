package com.sd_editions.collatex.spike2;

import java.util.Set;

import com.google.common.collect.Sets;

public class Colors {

  private final Set<String> colors;

  public Colors(String[] witnesses) {
    colors = Sets.newHashSet();
    for (String witness : witnesses) {
      WitnessTokenizer tokenizer = new WitnessTokenizer(witness);
      while (tokenizer.hasNextToken()) {
        colors.add(tokenizer.nextToken());
      }

    }
  }

  public int numberOfColors() {
    return colors.size();
  }
}
