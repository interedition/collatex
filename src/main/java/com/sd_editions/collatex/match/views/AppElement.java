package com.sd_editions.collatex.match.views;

public class AppElement extends Element {
  // TODO: rename!
  private final String addedWords;
  private final String reading;

  public AppElement(String addedWords) {
    this.addedWords = addedWords;
    this.reading = null;
  }

  public AppElement(String lemma, String reading) {
    addedWords = lemma;
    this.reading = reading;
  }

  // TODO: use StringBuilder!
  @Override
  public String toXML() {
    String result = "<app>";
    if (reading == null) {
      result += addedWords;
    } else {
      result += "<lemma>" + addedWords + "</lemma>";
      result += "<reading>" + reading + "</reading>";
    }
    result += "</app>";
    return result;
  }
}
