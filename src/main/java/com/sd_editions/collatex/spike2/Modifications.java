package com.sd_editions.collatex.spike2;

import java.util.List;

import com.sd_editions.collatex.spike2.collate.Transposition;

public class Modifications {
  private final List<Transposition> transpositions;

  public Modifications(List<Transposition> _transpositions) {
    this.transpositions = _transpositions;
  }

  public List<Transposition> getTranspositions() {
    return transpositions;
  }

}
