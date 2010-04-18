package com.sd_editions.collatex.match.views;

import com.google.common.base.Joiner;
import com.sd_editions.collatex.match.WordMatchMap;

public class WordMatchMapView {
  private final WordMatchMap map;

  public WordMatchMapView(WordMatchMap newMap) {
    this.map = newMap;
  }

  public String toHtml() {
    StringBuffer string = new StringBuffer("<table>");
    for (String word : map.getWords()) {
      string.append("<tr>");
      string.append("<th align=\"left\">&quot;" + word + "&quot;</th>");
      string.append("<td align=\"right\">exact matches</td><td>");
      string.append(Joiner.on(", ").join(map.getExactMatches(word)));
      string.append("</td></tr>");
      string.append("<tr><td colspan=\"2\" align=\"right\">Levenshtein matches</td><td>");
      string.append(Joiner.on(", ").join(map.getLevMatches(word)));
      string.append("</td></tr>");
    }
    string.append("</table>");
    return string.toString();
  }
}
