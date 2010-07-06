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

package com.sd_editions.collatex.Web;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.match.WordColorTuple;
import com.sd_editions.collatex.permutations.Matches;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class AlignmentView {
  private final CollateCore colors;

  public AlignmentView(CollateCore _colors) {
    this.colors = _colors;
  }

  public String toHtml() {
    Map<Word, Alignment> alignment = determineAlignment();
    StringBuilder html = new StringBuilder();
    for (int i = 0; i < colors.numberOfWitnesses(); i++) {
      Segment witness = colors.witnesses.get(i).getFirstSegment();
      html.append(colorWitness(alignment, witness));
    }
    return html.toString();
  }

  public Map<Word, Alignment> determineAlignment() {
    int colorCounter = 1;
    Map<Word, Alignment> wordToAlignment = Maps.newHashMap();
    for (int i = 1; i < colors.numberOfWitnesses(); i++) {
      for (int j = i + 1; j <= colors.numberOfWitnesses(); j++) {
        Matches matches = colors.getMatches(i, j);
        Set<Match<Word>> set = matches.permutations().get(0);
        for (Match<Word> match : set) {
          Word baseWord = match.getBaseWord();
          Alignment alignment = wordToAlignment.get(baseWord);
          if (alignment == null) {
            alignment = new Alignment(colorCounter++);
          }
          Word witnessWord = match.getWitnessWord();
          alignment.add(baseWord);
          alignment.add(witnessWord);
          wordToAlignment.put(baseWord, alignment);
          wordToAlignment.put(witnessWord, alignment);
        }
      }
    }
    return wordToAlignment;
  }

  private String colorWitness(Map<Word, Alignment> alignmentMap, Segment base) {
    StringBuilder html = new StringBuilder();
    List<String> words = Lists.newArrayList();
    for (Word word : base.getWords()) {
      Alignment alignment = alignmentMap.get(word);
      if (alignment != null) {
        words.add(new WordColorTuple(word.original, "color" + alignment.color).toHtml());
      } else {
        words.add(word.original);
      }
    }
    html.append(Joiner.on(" ").join(words));
    html.append("<br/>");

    return html.toString();
  }
}
