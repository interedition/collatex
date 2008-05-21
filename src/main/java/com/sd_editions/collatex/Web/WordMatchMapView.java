package com.sd_editions.collatex.Web;

import com.sd_editions.collatex.match_spike.WordCoordinate;
import com.sd_editions.collatex.match_spike.WordMatchMap;

public class WordMatchMapView {
  private final WordMatchMap map;

  public WordMatchMapView(WordMatchMap newMap) {
    this.map = newMap;
  }

  public String toHtml() {
    String string = "<table>";
    for (String word : map.getWords()) {
      string += "<tr>";
      string += "<th align=\"left\">&quot;" + word + "&quot;</th>";
      string += "<td align=\"right\">exact matches</td><td>";
      String sep = "";
      for (WordCoordinate c : map.getExactMatches(word)) {
        string += sep + c.toString();
        sep = ", ";
      }
      string += "</td></tr>";
      string += "<tr><td colspan=\"2\" align=\"right\">Levenshtein matches</td><td>";
      sep = "";
      for (WordCoordinate c : map.getLevMatches(word)) {
        string += sep + c.toString();
        sep = ", ";
      }
      string += "</td></tr>";
    }
    string += "</table>";
    return string;
  }

}
