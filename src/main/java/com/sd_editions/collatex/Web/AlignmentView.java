package com.sd_editions.collatex.Web;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.match.WordColorTuple;
import com.sd_editions.collatex.permutations.Matches;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.input.Witness;
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
      Witness witness = colors.witnesses.get(i);
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
        Set<Match> set = matches.permutations().get(0);
        for (Match match : set) {
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

  private String colorWitness(Map<Word, Alignment> alignmentMap, Witness base) {
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
    html.append(Join.join(" ", words));
    html.append("<br/>");

    return html.toString();
  }
}
