package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.spike2.collate.Replacement;
import com.sd_editions.collatex.spike2.collate.Transposition;

public class Modifications {
  private final List<Transposition> transpositions;
  private final List<Modification> modifications;
  private final List<Replacement> replacements;

  public Modifications(List<Modification> _modifications, List<Transposition> _transpositions, List<Replacement> _replacements) {
    this.modifications = _modifications;
    this.transpositions = _transpositions;
    this.replacements = _replacements;
  }

  public List<Transposition> getTranspositions() {
    return transpositions;
  }

  public List<Modification> getModifications() {
    List<Modification> addedUp = Lists.newArrayList();
    addedUp.addAll(modifications);
    addedUp.addAll(transpositions);
    addedUp.addAll(replacements);
    return addedUp;
  }

  public int size() {
    return getModifications().size();
  }

  public Modification get(int i) {
    return getModifications().get(i);
  }

  public List<Replacement> getReplacements() {
    return replacements;
  }

}
