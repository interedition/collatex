package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Word;

public class Omission extends Cell {
  private final Word base;

  public Omission(Word base) {
    this.base = base;
  }

  @Override
  public String toString() {
    return "omission: " + base.getContent();
  }

  @Override
  public String toHTML() {
    return "{" + base.getContent() + "}";
  }

  @Override
  public String getType() {
    return "omission";
  }

}
