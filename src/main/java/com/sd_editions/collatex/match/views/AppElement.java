package com.sd_editions.collatex.match.views;

public class AppElement extends Element {

  private final String addedWords;

  public AppElement(String addedWords) {
    this.addedWords = addedWords;
  }

  // TODO: use StringBuilder!
  @Override
  public String toXML() {
    String result = "<app>";
    result += addedWords;
    result += "</app>";
    return result;
  }

}
