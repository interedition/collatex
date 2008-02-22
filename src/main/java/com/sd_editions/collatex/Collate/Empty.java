package com.sd_editions.collatex.Collate;

public class Empty extends Cell {
  private static Cell instance;

  private Empty() {

  }

  public static synchronized Cell getInstance() {
    if (instance == null) {
      instance = new Empty();
    }
    return instance;
  }

  @Override
  public String toHTML() {
    return "&nbsp;";
  }

  @Override
  public String getType() {
    return "empty";
  }

}
