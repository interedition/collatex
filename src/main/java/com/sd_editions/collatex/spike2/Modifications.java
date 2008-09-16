package com.sd_editions.collatex.spike2;

import java.util.List;

import com.sd_editions.collatex.spike2.collate.Transposition;

public class Modifications {
  private final List<Transposition> transpositions;
  private final List<Modification> modifications;

  public Modifications(List<Modification> _modifications, List<Transposition> _transpositions) {
    this.modifications = _modifications;
    this.transpositions = _transpositions;
  }

  public List<Transposition> getTranspositions() {
    return transpositions;
  }

  public List<Modification> getModifications() {
    return modifications;
  }

  public int size() {
    return modifications.size();
  }

  public Modification get(int i) {
    return modifications.get(i);
  }

}
