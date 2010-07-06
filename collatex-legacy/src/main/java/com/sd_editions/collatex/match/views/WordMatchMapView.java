/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
